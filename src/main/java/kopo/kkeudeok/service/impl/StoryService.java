package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.MissionType;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.dto.StoryDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryNodeMeta;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.dto.StoryScenarioDTO;
import kopo.kkeudeok.dto.StorySessionDTO;
import kopo.kkeudeok.dto.StoryStage;
import kopo.kkeudeok.mapper.MissionLogMapper;
import kopo.kkeudeok.mapper.StoryMapper;
import kopo.kkeudeok.mapper.StorySessionMapper;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.service.IRoadmapService;
import kopo.kkeudeok.service.IStoryAiService;
import kopo.kkeudeok.service.IStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// 학습 세션 진행
@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService implements IStoryService {

    private final StoryMapper storyMapper;
    private final StorySessionMapper sessionMapper;
    private final MissionLogMapper missionLogMapper;

    private final IChildService childService;
    private final IStoryAiService storyAiService;
    private final IRoadmapService roadmapService;
    private final ObjectMapper objectMapper;
    private final org.springframework.context.ApplicationEventPublisher publisher;

    @Value("${kkeudeok.story.daily-goal:3}")
    private int dailyGoal;

    private static final List<String> PLACES = List.of(
            "유치원 교실", "놀이터 미끄럼틀 앞", "급식실", "친구 생일잔치",
            "도서관 그림책 코너", "등원길 버스 안", "운동장", "미술 시간",
            "집 거실", "문구점", "물놀이터", "체육관");

    private static final Map<String, String> EMOTION_KEYS = Map.of(
            "기쁨", "happy",
            "슬픔", "sad",
            "화남", "angry",
            "놀람", "surprise"
    );

    @Override
    @Transactional
    public StoryResponseDTO.Start start(StoryRequestDTO.Start req) {

        ChildDTO child = childService.getChild(req.getChildId());

        if (req.getEmotion() == null || req.getEmotion().isBlank()) {

            StorySessionDTO ready = sessionMapper.selectPrepared(child.getChildId());

            if (ready != null) {
                return adopt(ready, child);
            }
        }

        RoadmapDTO roadmap = roadmapService.getOrCreate(child.getChildId());
        RoadmapPlanDTO.Week week = roadmapService.currentWeekPlan(roadmap);

        int madeSoFar = storyMapper.countStories(child.getChildId());
        String lastEmotion = storyMapper.selectLastEmotion(child.getChildId());

        String emotion = emotionFor(req, week, madeSoFar, lastEmotion);

        List<String> recent = storyMapper.selectRecentTitles(child.getChildId(), 8);

        boolean weekOpener = isWeekOpener(child.getChildId(), roadmap, week);

        String note = trimToNull(req.getDailyInput());
        String place = (note != null || weekOpener) ? null
                : PLACES.get(Math.floorMod(madeSoFar, PLACES.size()));

        IStoryAiService.Brief brief = new IStoryAiService.Brief(
                place, recent, laterTopics(roadmap, week), weekOpener);

        StoryScenarioDTO scenario =
                storyAiService.createScenario(child, emotion, req.getDailyInput(), brief, week);

        if (scenario == null) {
            throw new IllegalStateException("아직 이야기를 만들지 못했습니다. 잠시 뒤 다시 시도해 주세요");
        }

        String source = scenario.getSource() == null ? "AI" : scenario.getSource();

        StoryDTO story = StoryDTO.builder()
                .childId(child.getChildId())
                .title(scenario.getTitle())
                .situationType(SituationType.normalize(scenario.getSituationType(), emotion).label())
                .emotion(emotion)                    
                .isGenerated("AI".equals(source))
                .build();

        storyMapper.insertStory(story);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(child.getChildId())
                .storyId(story.getStoryId())
                .roadmapId(roadmap == null ? null : roadmap.getRoadmapId())
                .dailyInput(trimToNull(req.getDailyInput()))
                .status(StorySessionDTO.INCOMPLETE)

                .prepared(req.isPrepare())
                .build();

        sessionMapper.insertSession(session);

        StoryNodeDTO node = buildWholeStory(story.getStoryId(), child, scenario);

        int storySeq = sessionMapper.countTodaySessions(child.getChildId());

        log.info("학습 세션 시작 — sessionId={}, storyId={}, child={}, 오늘 {}번째, 감정={}, 출처={}",
                session.getSessionId(), story.getStoryId(), child.getChildId(), storySeq, emotion, source);

        return StoryResponseDTO.Start.builder()
                .sessionId(session.getSessionId())
                .storyId(story.getStoryId())
                .storySeq(storySeq)
                .dailyGoal(dailyGoal)
                .emotion(emotion)
                .title(story.getTitle())
                .source(source)
                .childCallName(child.getCallName())
                .characterKey(child.getCharacterType())
                .node(node)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponseDTO.Resume resume(Long childId) {

        ChildDTO child = childService.getChild(childId);
        StorySessionDTO session = sessionMapper.selectResumable(child.getChildId());

        if (session == null) {
            return StoryResponseDTO.Resume.builder()
                    .found(false)
                    .dailyGoal(dailyGoal)
                    .build();
        }

        List<StoryNodeDTO> nodes = storyMapper.selectNodes(session.getStoryId());

        if (nodes.isEmpty()) {
            log.warn("세션 {} 에 노드가 없어 이어하기를 건너뜁니다", session.getSessionId());
            return StoryResponseDTO.Resume.builder()
                    .found(false)
                    .dailyGoal(dailyGoal)
                    .build();
        }

        String answered = missionLogMapper.selectLastAnsweredStage(session.getSessionId());

        if (answered == null) {
            sessionMapper.updateSessionClosed(session.getSessionId(), StorySessionDTO.INCOMPLETE);

            log.info("답한 기록이 없어 세션 {} 을 닫고 새 이야기로 갑니다", session.getSessionId());

            return StoryResponseDTO.Resume.builder()
                    .found(false)
                    .dailyGoal(dailyGoal)
                    .build();
        }

        StoryStage resumeStage = StoryStage.of(answered)
                .flatMap(StoryStage::next)
                .orElse(StoryStage.first());

        StoryNodeDTO resumeNode = storyMapper.selectNodeByOrder(session.getStoryId(), resumeStage.seq());

        if (resumeNode == null) {

            resumeNode = nodes.get(0);
            resumeStage = StoryStage.of(resumeNode.getStageType()).orElse(StoryStage.first());
        }

        StoryNodeMeta.unpack(resumeNode, objectMapper);

        StoryDTO story = storyMapper.selectStory(session.getStoryId());

        String screen = resumeStage.screenKey();

        log.info("이어할 학습을 찾았습니다 — sessionId={}, 답한 단계={}, {}단계부터",
                session.getSessionId(), answered, resumeStage);

        return StoryResponseDTO.Resume.builder()
                .found(true)
                .sessionId(session.getSessionId())
                .storyId(session.getStoryId())
                .storySeq(sessionMapper.countTodaySessions(child.getChildId()))
                .dailyGoal(dailyGoal)
                .title(story == null ? null : story.getTitle())
                .childCallName(child.getCallName())
                .characterKey(child.getCharacterType())
                .resumeScreen(screen)
                .node(resumeNode)
                .build();
    }

    @Override
    @Transactional
    public StoryResponseDTO.Next next(Long sessionId, StoryRequestDTO.Next req) {

        StorySessionDTO session = requireOpenSession(sessionId);

        StoryStage current = StoryStage.of(req.getStageType())
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 단계입니다: " + req.getStageType()));

        StoryStage nextStage = current.next()
                .orElseThrow(() -> new IllegalStateException("칭찬이 마지막 단계입니다"));

        ChildDTO child = childService.getChild(session.getChildId());
        StoryScenarioDTO scenario = readScenario(session);

        StoryNodeDTO exist = storyMapper.selectNodeByOrder(session.getStoryId(), nextStage.seq());

        StoryNodeDTO node;

        if (exist != null) {
            StoryNodeMeta.unpack(exist, objectMapper);
            node = exist;
        } else {
            node = buildAndSaveNode(session.getStoryId(), child, scenario, nextStage, req);
        }

        publisher.publishEvent(new NodeReady(sessionId, nextStage.name()));

        return StoryResponseDTO.Next.builder()
                .node(node)
                .last(nextStage.next().isEmpty())
                .build();
    }

    private boolean isWeekOpener(Long childId, RoadmapDTO roadmap, RoadmapPlanDTO.Week week) {

        if (roadmap == null || roadmap.getCreatedAt() == null || week == null) {
            return false;
        }

        LocalDateTime weekStart = roadmap.getCreatedAt().plusDays(7L * (week.getNo() - 1));

        return storyMapper.countStoriesSince(childId, weekStart) == 0;
    }

    private static final int LOOK_AHEAD_WEEKS = 5;

    private List<String> laterTopics(RoadmapDTO roadmap, RoadmapPlanDTO.Week week) {

        if (roadmap == null || roadmap.getPlan() == null
                || roadmap.getPlan().getWeeks() == null || week == null) {
            return List.of();
        }

        List<String> out = new ArrayList<>();

        for (RoadmapPlanDTO.Week w : roadmap.getPlan().getWeeks()) {

            if (w == null || w.getNo() <= week.getNo() || w.getTopic() == null) {
                continue;
            }
            if (w.getNo() > week.getNo() + LOOK_AHEAD_WEEKS) {
                break;
            }
            out.add(w.getTopic());
        }

        return out;
    }

    private StoryResponseDTO.Start adopt(StorySessionDTO ready, ChildDTO child) {

        sessionMapper.markPreparedUsed(ready.getSessionId());

        StoryDTO story = storyMapper.selectStory(ready.getStoryId());
        StoryNodeDTO first = storyMapper.selectNodeByOrder(ready.getStoryId(), StoryStage.STORY.seq());

        StoryNodeMeta.unpack(first, objectMapper);

        int storySeq = sessionMapper.countTodaySessions(child.getChildId());

        log.info("미리 만들어 둔 이야기를 씁니다 — sessionId={}, storyId={}, child={}, 감정={}",
                ready.getSessionId(), ready.getStoryId(), child.getChildId(),
                story == null ? "?" : story.getEmotion());

        return StoryResponseDTO.Start.builder()
                .sessionId(ready.getSessionId())
                .storyId(ready.getStoryId())
                .storySeq(storySeq)
                .dailyGoal(dailyGoal)
                .emotion(story == null ? null : story.getEmotion())
                .title(story == null ? null : story.getTitle())
                .source("AI")
                .childCallName(child.getCallName())
                .characterKey(child.getCharacterType())
                .node(first)
                .build();
    }

    public record NodeReady(Long sessionId, String stageType) {
    }

    @Override
    @Transactional
    public void prefetchNext(Long sessionId, String fromStage) {

        StoryStage cur = StoryStage.of(fromStage).orElse(null);
        if (cur == null || cur.next().isEmpty()) {
            return;                                  
        }

        StorySessionDTO session = sessionMapper.selectSession(sessionId);
        if (session == null) {
            return;
        }

        StoryStage nextStage = cur.next().get();

        if (storyMapper.selectNodeByOrder(session.getStoryId(), nextStage.seq()) != null) {
            return;                                  
        }

        try {
            ChildDTO child = childService.getChild(session.getChildId());
            buildAndSaveNode(session.getStoryId(), child, readScenario(session), nextStage, null);

            log.info("다음 노드를 미리 만들어 뒀습니다 — session={}, {}", sessionId, nextStage);

        } catch (Exception e) {

            log.warn("미리 만들기 실패 — session={}, {}: {}", sessionId, nextStage, e.getMessage());
        }
    }

    @Override
    @Transactional
    public StoryResponseDTO.Finish finish(Long sessionId, StoryRequestDTO.Finish req) {

        StorySessionDTO session = sessionMapper.selectSession(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("없는 세션입니다: " + sessionId);
        }

        List<MissionLogDTO> rows = toLogRows(session, req.getResults());

        if (!rows.isEmpty()) {
            missionLogMapper.deleteByNodes(sessionId, rows.stream().map(MissionLogDTO::getNodeId).toList());
            missionLogMapper.insertLogs(rows);
        }

        String status = req.isCompleted() ? StorySessionDTO.COMPLETED : StorySessionDTO.INCOMPLETE;
        sessionMapper.updateSessionClosed(sessionId, status);

        int todayDone = sessionMapper.countTodayCompleted(session.getChildId());

        log.info("학습 세션 종료 — sessionId={}, {}, 결과 {}건, 오늘 마친 편수 {}",
                sessionId, status, rows.size(), todayDone);

        return StoryResponseDTO.Finish.builder()
                .sessionId(sessionId)
                .status(status)
                .savedCount(rows.size())
                .todayDone(todayDone)
                .dailyGoal(dailyGoal)
                .build();
    }

    private StoryNodeDTO buildWholeStory(Long storyId, ChildDTO child, StoryScenarioDTO scenario) {

        StoryNodeDTO first = null;

        for (StoryStage stage : StoryStage.values()) {

            StoryNodeDTO made = buildAndSaveNode(storyId, child, scenario, stage, null);

            if (stage == StoryStage.STORY) {
                first = made;
            }
        }

        log.info("이야기 {} — {}개 화면을 한꺼번에 만들었습니다", storyId, StoryStage.values().length);

        return first;
    }

    private StoryNodeDTO buildAndSaveNode(Long storyId,
                                          ChildDTO child,
                                          StoryScenarioDTO scenario,
                                          StoryStage stage,
                                          StoryRequestDTO.Next previous) {

        StoryNodeDTO node = storyAiService.writeNode(child, scenario, stage, previous);

        node.setStoryId(storyId);
        node.setNodeOrder(stage.seq());
        node.setStageType(stage.name());
        node.setMissionType(stage.hasMission() ? stage.defaultMission().name() : null);

        if (node.getNarration() == null || node.getNarration().isBlank()) {
            node.setNarration(node.getTitle() == null ? "" : node.getTitle());
        }

        if (stage == StoryStage.STORY) {
            node.setScenario(scenario);
        }

        StoryNodeMeta.pack(node, objectMapper);

        try {
            storyMapper.insertNode(node);

        } catch (DuplicateKeyException e) {

            StoryNodeDTO exist = storyMapper.selectNodeByOrderLive(storyId, stage.seq());

            if (exist == null) {
                throw e;                            
            }

            log.info("{}번 노드는 이미 만들어져 있어 그것을 씁니다 — story={}", stage.seq(), storyId);

            StoryNodeMeta.unpack(exist, objectMapper);
            return exist;
        }

        return node;
    }

    private List<MissionLogDTO> toLogRows(StorySessionDTO session,
                                          List<StoryRequestDTO.Finish.Item> items) {

        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<MissionLogDTO> rows = new ArrayList<>(items.size());

        for (StoryRequestDTO.Finish.Item item : items) {

            if (item == null || item.getNodeOrder() == null) {
                continue;
            }

            StoryNodeDTO node = storyMapper.selectNodeByOrder(session.getStoryId(), item.getNodeOrder());

            if (node == null) {
                log.warn("이야기 {} 에 {}번 노드가 없어 결과를 버립니다", session.getStoryId(), item.getNodeOrder());
                continue;
            }

            StoryNodeMeta.unpack(node, objectMapper);

            if (!node.hasMission()) {
                log.debug("{}번 노드는 미션이 없어 결과를 버립니다", item.getNodeOrder());
                continue;
            }

            String missionType = MissionType.of(item.getMissionType())
                    .map(Enum::name)
                    .orElse(node.getMissionType());

            rows.add(MissionLogDTO.builder()
                    .sessionId(session.getSessionId())
                    .nodeId(node.getNodeId())
                    .missionType(missionType)
                    .targetValue(cut(node.getTargetValue(), 20, ""))
                    .responseValue(cut(item.getResponseValue(), 50, "(반응 없음)"))
                    .isSuccess(item.isSuccess())
                    .build());
        }

        return rows;
    }

    private StorySessionDTO requireOpenSession(Long sessionId) {

        StorySessionDTO session = sessionMapper.selectSession(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("없는 세션입니다: " + sessionId);
        }

        if (StorySessionDTO.COMPLETED.equals(session.getStatus())) {
            throw new IllegalArgumentException("이미 끝난 세션입니다: " + sessionId);
        }

        return session;
    }

    private StoryScenarioDTO readScenario(StorySessionDTO session) {

        StoryNodeDTO first = storyMapper.selectNodeByOrder(session.getStoryId(), StoryStage.STORY.seq());

        if (first != null) {
            StoryNodeMeta.unpack(first, objectMapper);
            if (first.getScenario() != null) {
                return first.getScenario();
            }
        }

        throw new IllegalStateException(
                "세션 " + session.getSessionId() + " 의 이야기 씨앗을 찾지 못했습니다");
    }

    private String emotionFor(StoryRequestDTO.Start req, RoadmapPlanDTO.Week week, int seq, String last) {

        if (req.getEmotion() != null && !req.getEmotion().isBlank()) {
            return normalizeEmotion(req.getEmotion());   
        }

        if (week != null && week.getSituationType() != null) {
            SituationType type = SituationType.normalize(week.getSituationType(), null);

            List<String> all = type.emotions();
            int at = Math.floorMod(week.getNo() + seq, all.size());
            String emo = all.get(at);

            if (emo.equals(last) && all.size() > 1) {
                emo = all.get((at + 1) % all.size());
            }

            log.info("이번 주 상황 '{}' · {}번째 이야기 → 감정 {} (직전 {})",
                    week.getSituationType(), seq + 1, emo, last == null ? "없음" : last);
            return emo;
        }

        return "sad";
    }

    private String normalizeEmotion(String raw) {

        if (raw == null || raw.isBlank()) {
            return "sad";
        }

        String v = raw.trim();
        String mapped = EMOTION_KEYS.get(v);

        if (mapped != null) {
            return mapped;
        }

        String lower = v.toLowerCase(Locale.ROOT);

        return switch (lower) {
            case "happy", "sad", "angry", "surprise" -> lower;
            default -> "sad";
        };
    }

    private String cut(String value, int max, String whenEmpty) {

        if (value == null || value.isBlank()) {
            return whenEmpty;
        }

        String v = value.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }

    private static String trimToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponseDTO.Summary summary(Long childId) {

        List<StorySessionDTO> rows = sessionMapper.selectRecent(childId, RECENT_LIMIT);

        List<StoryResponseDTO.Summary.Record> recent = new ArrayList<>(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            StorySessionDTO r = rows.get(i);

            recent.add(StoryResponseDTO.Summary.Record.builder()
                    .no(i + 1)
                    .date(dayLabel(r.getStartedAt()))
                    .title(r.getStoryTitle())
                    .completed(StorySessionDTO.COMPLETED.equals(r.getStatus()))
                    .build());
        }

        List<LocalDate> days = sessionMapper.selectActiveDays(childId, STREAK_LOOKBACK);

        return StoryResponseDTO.Summary.builder()
                .streakDays(streakOf(days))
                .activeDays(days.stream().map(LocalDate::toString).toList())
                .todayDone(sessionMapper.countTodayCompleted(childId))
                .dailyGoal(dailyGoal)
                .recent(recent)
                .build();
    }

    private static final int RECENT_LIMIT = 5;

    private static final int STREAK_LOOKBACK = 400;

    private int streakOf(List<LocalDate> days) {

        if (days == null || days.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate first = days.get(0);

        if (first.isBefore(today.minusDays(1))) {
            return 0;                       
        }

        int n = 1;
        LocalDate prev = first;

        for (int i = 1; i < days.size(); i++) {
            if (!days.get(i).equals(prev.minusDays(1))) {
                break;                      
            }
            prev = days.get(i);
            n += 1;
        }

        return n;
    }

    private String dayLabel(LocalDateTime at) {

        if (at == null) {
            return "-";
        }

        LocalDate d = at.toLocalDate();
        LocalDate today = LocalDate.now();

        if (d.equals(today)) return "오늘";
        if (d.equals(today.minusDays(1))) return "어제";

        return d.getMonthValue() + "." + d.getDayOfMonth();
    }
}
