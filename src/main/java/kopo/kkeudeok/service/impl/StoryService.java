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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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

    @Value("${kkeudeok.story.daily-goal:3}")
    private int dailyGoal;

    private static final Map<String, String> EMOTION_KEYS = Map.of(
            "기쁨", "happy",
            "슬픔", "sad",
            "화남", "angry",
            "놀람", "surprise"
    );

    // ------------------------------------------------------------
    //  1) 세션 시작
    // ------------------------------------------------------------

    @Override
    @Transactional
    public StoryResponseDTO.Start start(StoryRequestDTO.Start req) {

        ChildDTO child = childService.getChild(req.getChildId());
        String emotion = normalizeEmotion(req.getEmotion());

        RoadmapDTO roadmap = roadmapService.getOrCreate(child.getChildId());
        RoadmapPlanDTO.Week week = roadmapService.currentWeekPlan(roadmap);

        StoryScenarioDTO scenario = storyAiService.createScenario(child, emotion, req.getDailyInput(), week);
        String source = scenario.getSource() == null ? "FALLBACK" : scenario.getSource();

        StoryDTO story = StoryDTO.builder()
                .childId(child.getChildId())
                .title(scenario.getTitle())
                .situationType(SituationType.normalize(scenario.getSituationType(), emotion).label())
                .isGenerated("AI".equals(source))
                .build();

        storyMapper.insertStory(story);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(child.getChildId())
                .storyId(story.getStoryId())
                .roadmapId(roadmap == null ? null : roadmap.getRoadmapId())
                .dailyInput(trimToNull(req.getDailyInput()))
                .status(StorySessionDTO.INCOMPLETE)
                .build();

        sessionMapper.insertSession(session);

        StoryNodeDTO node = buildAndSaveNode(story.getStoryId(), child, scenario, StoryStage.STORY, null);

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

    // ------------------------------------------------------------
    //  1-2) 이어하기
    // ------------------------------------------------------------

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

        /* 어디서부터 이어할지는 **아이가 답한 것**으로 정한다.
           마지막으로 만들어진 노드를 쓰면 안 된다 — 화면이 다음 노드를 미리 받아 두므로
           (story-session.js 의 prefetch) 아이가 보지도 않은 단계가 마지막 노드가 된다.
           그래서 학습을 시작하자마자 이야기 화면이 통과되고 마음 화면으로 넘어갔다
           (2026-08-14 지적). 답한 단계가 없으면 처음(이야기)부터가 맞다. */
        String answered = missionLogMapper.selectLastAnsweredStage(session.getSessionId());

        StoryStage resumeStage = StoryStage.of(answered)
                .flatMap(StoryStage::next)      // 답한 단계는 끝난 것 — 그 다음부터
                .orElse(StoryStage.first());

        StoryNodeDTO resumeNode = storyMapper.selectNodeByOrder(session.getStoryId(), resumeStage.seq());

        if (resumeNode == null) {
            // 이어할 단계의 노드가 아직 없다(있을 수 없지만 방어) — 처음부터 다시 그린다
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

    // ------------------------------------------------------------
    //  2) 다음 노드
    // ------------------------------------------------------------

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

        return StoryResponseDTO.Next.builder()
                .node(node)
                .last(nextStage.next().isEmpty())
                .build();
    }

    // ------------------------------------------------------------
    //  3) 세션 종료 — 결과 일괄 저장
    // ------------------------------------------------------------

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

    // ------------------------------------------------------------
    //  내부
    // ------------------------------------------------------------
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
        storyMapper.insertNode(node);

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

        log.warn("세션 {} 의 시나리오 씨앗을 찾지 못해 내장 시나리오로 잇습니다", session.getSessionId());

        return FallbackStory.scenario("sad", "토리");
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
}
