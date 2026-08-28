package kopo.kkeudeok.service;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.dto.StoryDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryStage;
import kopo.kkeudeok.dto.StoryOptionDTO;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.dto.StorySessionDTO;
import kopo.kkeudeok.mapper.IMissionLogMapper;
import kopo.kkeudeok.mapper.IStoryMapper;
import kopo.kkeudeok.mapper.IStorySessionMapper;
import kopo.kkeudeok.service.impl.GeminiStoryAiService;
import kopo.kkeudeok.service.impl.StoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoryServiceTest {

    private StoryService storyService;

    private final Map<String, StoryNodeDTO> savedNodes = new HashMap<>();

    private final List<MissionLogDTO> savedLogs = new ArrayList<>();

    private StorySessionDTO savedSession;
    private StoryDTO savedStory;

    private StorySessionDTO resumable;
    private StorySessionDTO prepared;

    private String answeredStage;

    private final List<Long> deletedNodeIds = new ArrayList<>();

    private IRoadmapService roadmapService;

    private StoryResponseDTO.Start startSad() {
        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("슬픔");
        return storyService.start(req);
    }

    private int todaySessions = 1;
    private int storyCount = 0;
    private String lastEmotion;

    private IStoryMapper storyMapperRef;
    private GeminiClient gemini;

    @BeforeEach
    void setUp() {

        gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(true);
        when(gemini.generateJson(anyString(), anyString())).thenReturn(Optional.of("""
                {"title":"토리가 슬퍼요","emotion":"sad","situationType":"감정 표현하기",
                 "situation":"토리가 장난감을 잃어버렸어요","cause":"장난감을 잃어버려서",
                 "causeDistractor":"졸려서","gesture":"comfort","praise":"고마워, 마음이 나아졌어"}
                """));

        ObjectMapper objectMapper = new ObjectMapper();

        kopo.kkeudeok.config.GeminiProperties props = new kopo.kkeudeok.config.GeminiProperties();

        IStoryAiService aiService = new GeminiStoryAiService(gemini, props, objectMapper);
        IChildService childService = mock(IChildService.class);
        when(childService.getChild(any())).thenReturn(ChildDTO.builder()
                .childId(1L)
                .name("김지우")
                .birthDate(LocalDate.now().minusYears(6))
                .disorderType("자폐")
                .severity("경도")
                .characterType("tori")
                .build());

        IStoryMapper storyMapper = mock(IStoryMapper.class);
        storyMapperRef = storyMapper;
        IStorySessionMapper sessionMapper = mock(IStorySessionMapper.class);
        IMissionLogMapper logMapper = mock(IMissionLogMapper.class);
        when(storyMapper.insertStory(any())).thenAnswer(inv -> {
            savedStory = inv.getArgument(0);
            savedStory.setStoryId(50L);
            return 1;
        });
        when(storyMapper.selectStory(any())).thenAnswer(inv -> savedStory);

        AtomicLong nodeId = new AtomicLong(1000);

        when(storyMapper.insertNode(any())).thenAnswer(inv -> {
            StoryNodeDTO n = inv.getArgument(0);
            n.setNodeId(nodeId.incrementAndGet());
            savedNodes.put(n.getStoryId() + ":" + n.getNodeOrder(), n);
            return 1;
        });

        when(storyMapper.selectNodeByOrder(any(), any())).thenAnswer(inv ->
                savedNodes.get(inv.getArgument(0) + ":" + inv.getArgument(1)));

        when(sessionMapper.insertSession(any())).thenAnswer(inv -> {
            savedSession = inv.getArgument(0);
            savedSession.setSessionId(100L);
            return 1;
        });
        when(sessionMapper.selectSession(any())).thenAnswer(inv -> savedSession);
        when(sessionMapper.countTodaySessions(any())).thenAnswer(inv -> todaySessions);
        when(storyMapper.countStories(any())).thenAnswer(inv -> storyCount);
        when(storyMapper.selectLastEmotion(any())).thenAnswer(inv -> lastEmotion);
        when(sessionMapper.countTodayCompleted(any())).thenReturn(1);

        when(storyMapper.selectNodes(any())).thenAnswer(inv -> savedNodes.values().stream()
                .filter(n -> n.getStoryId().equals(inv.getArgument(0)))
                .sorted(java.util.Comparator.comparing(StoryNodeDTO::getNodeOrder))
                .toList());

        when(sessionMapper.selectResumable(any())).thenAnswer(inv -> resumable);
        when(sessionMapper.selectPrepared(any())).thenAnswer(inv -> prepared);
        when(logMapper.selectLastAnsweredStage(any())).thenAnswer(inv -> answeredStage);

        when(logMapper.insertLogs(any())).thenAnswer(inv -> {
            savedLogs.addAll(inv.getArgument(0));
            return savedLogs.size();
        });

        when(logMapper.deleteByNodes(any(), any())).thenAnswer(inv -> {
            deletedNodeIds.addAll(inv.getArgument(1));
            return 0;
        });

        roadmapService = mock(IRoadmapService.class);
        when(roadmapService.getOrCreate(any())).thenReturn(RoadmapDTO.builder()
                .roadmapId(700L)
                .childId(1L)
                .roadmapType(RoadmapDTO.TYPE_AI)
                .isActive(true)
                .build());
        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1)
                .stage("감정 표현 집중")
                .topic("표정으로 표현하기")
                .situationType(SituationType.EXPRESS.label())
                .goal("기쁨과 슬픔을 표정으로 나타내 봐요")
                .build());

        storyService = new StoryService(storyMapper, sessionMapper, logMapper,
                childService, aiService, roadmapService, objectMapper, event -> { });

        ReflectionTestUtils.setField(storyService, "dailyGoal", 3);
    }

    @Test
    @DisplayName("세션을 시작하면 이야기가 먼저 만들어지고 세션이 그것을 가리킨다")
    void start() {

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("슬픔");
        req.setDailyInput("놀이터에서 친구를 못 만났어요");

        StoryResponseDTO.Start res = storyService.start(req);

        assertThat(res.sessionId()).isEqualTo(100L);
        assertThat(res.storyId()).isEqualTo(50L);
        assertThat(res.emotion()).isEqualTo("sad");
        assertThat(res.storySeq()).isEqualTo(1);
        assertThat(res.dailyGoal()).isEqualTo(3);
        assertThat(res.childCallName()).isEqualTo("지우");
        assertThat(res.source()).isEqualTo("AI");
        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.EXPRESS.label());
        assertThat(savedStory.getChildId()).isEqualTo(1L);
        assertThat(savedStory.getIsGenerated()).isTrue();

        assertThat(savedSession.getStoryId()).isEqualTo(50L);
        assertThat(savedSession.getDailyInput()).isEqualTo("놀이터에서 친구를 못 만났어요");
        assertThat(savedSession.getStatus()).isEqualTo("INCOMPLETE");

        StoryNodeDTO node = res.node();
        assertThat(node.getStageType()).isEqualTo("STORY");
        assertThat(node.getNodeOrder()).isEqualTo(1);
        assertThat(node.getMissionType()).isNull();
        assertThat(node.getTitle()).contains("토리");
        assertThat(node.getNarration()).isNotBlank();
        assertThat(node.getChoiceData()).contains("scenario");
    }

    @Test
    @DisplayName("오늘의 일상을 안 넣어도 프로필만으로 이야기가 만들어진다")
    void startWithoutDailyInput() {

        StoryResponseDTO.Start res = startSad();

        assertThat(res.node().getTitle()).isNotBlank();
        assertThat(savedSession.getDailyInput()).isNull();
    }

    @Test
    @DisplayName("STORY 다음은 MIND — 선택지 3장이 붙고 정답은 하나다")
    void nextToMind() {

        startSad();

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        StoryResponseDTO.Next res = storyService.next(100L, 1L, req);
        StoryNodeDTO node = res.node();

        assertThat(node.getStageType()).isEqualTo("MIND");
        assertThat(node.getNodeOrder()).isEqualTo(2);
        assertThat(node.getMissionType()).isEqualTo("CHOICE");
        assertThat(node.getTargetValue()).isEqualTo("sad");
        assertThat(res.last()).isFalse();

        assertThat(node.getOptions()).hasSize(3);
        assertThat(node.getOptions()).filteredOn(StoryOptionDTO::isAnswer).hasSize(1);
        assertThat(node.getOptions()).filteredOn(StoryOptionDTO::isAnswer)
                .first().extracting(StoryOptionDTO::getKey).isEqualTo("sad");
    }

    @Test
    @DisplayName("6단계를 끝까지 넘기면 마지막이 칭찬이고 last 가 켜진다")
    void walkAllStages() {

        startSad();

        StoryResponseDTO.Next res = null;

        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            req.setSuccess(true);
            res = storyService.next(100L, 1L, req);
        }

        assertThat(res).isNotNull();
        assertThat(res.node().getStageType()).isEqualTo("PRAISE");
        assertThat(res.node().getNodeOrder()).isEqualTo(6);
        assertThat(res.node().getMissionType()).isNull();
        assertThat(res.last()).isTrue();

        StoryRequestDTO.Next after = new StoryRequestDTO.Next();
        after.setStageType("PRAISE");
        assertThatThrownBy(() -> storyService.next(100L, 1L, after))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("같은 단계를 두 번 요청해도 이야기가 바뀌지 않는다")
    void nextIsIdempotent() {

        startSad();

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        StoryNodeDTO first = storyService.next(100L, 1L, req).node();
        StoryNodeDTO again = storyService.next(100L, 1L, req).node();

        assertThat(again.getNodeId()).isEqualTo(first.getNodeId());
        assertThat(again.getTitle()).isEqualTo(first.getTitle());
        assertThat(again.getMissionType()).isEqualTo(first.getMissionType());
    }

    @Test
    @DisplayName("세션 종료 — 결과가 노드에 붙고 미션 없는 단계는 버려진다")
    void finish() {

        startSad();

        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            storyService.next(100L, 1L, req);
        }

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(true);
        finish.setResults(List.of(
                item(2, "CHOICE", "angry", false),
                item(3, "VOICE", "넘어져서 아파서", true),
                item(4, "EXPRESSION", "sad", true),
                item(5, "GESTURE", "comfort", true),
                item(1, "CHOICE", "무시됨", true),
                item(99, "CHOICE", "없는 노드", true)
        ));

        StoryResponseDTO.Finish res = storyService.finish(100L, 1L, finish);
        assertThat(res.status()).isEqualTo("COMPLETED");
        assertThat(res.savedCount()).isEqualTo(4);
        assertThat(res.dailyGoal()).isEqualTo(3);
        assertThat(res.todayDone()).isEqualTo(1);

        assertThat(savedLogs).hasSize(4);
        MissionLogDTO mind = savedLogs.get(0);
        assertThat(mind.getMissionType()).isEqualTo("CHOICE");
        assertThat(mind.getTargetValue()).isEqualTo("sad");
        assertThat(mind.getResponseValue()).isEqualTo("angry");
        assertThat(mind.getIsSuccess()).isFalse();
        assertThat(mind.getNodeId()).isNotNull();

        assertThat(savedLogs.get(1).getMissionType()).isEqualTo("VOICE");

        assertThat(savedLogs).allSatisfy(l -> {
            assertThat(l.getTargetValue()).hasSizeLessThanOrEqualTo(20);
            assertThat(l.getResponseValue()).hasSizeLessThanOrEqualTo(50);
        });
    }

    @Test
    @DisplayName("중도 이탈은 미완료로 남고 그때까지의 결과는 저장된다")
    void abandon() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, 1L, req);

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(false);
        finish.setResults(List.of(item(2, "CHOICE", "sad", true)));

        StoryResponseDTO.Finish res = storyService.finish(100L, 1L, finish);

        assertThat(res.status()).isEqualTo("INCOMPLETE");
        assertThat(res.savedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("긴 음성 문장은 컬럼 길이에 맞춰 잘린다")
    void longVoiceIsTrimmed() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, 1L, req);

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(true);
        finish.setResults(List.of(item(2, "VOICE", "아".repeat(300), true)));

        storyService.finish(100L, 1L, finish);

        assertThat(savedLogs.get(0).getResponseValue()).hasSize(50);
    }

    @Test
    @DisplayName("story.situation_type 에는 감정이 아니라 사회적 상황이 들어간다")
    void situationTypeIsNotEmotion() {

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("화남");

        storyService.start(req);

        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.EXPRESS.label());
        assertThat(savedStory.getSituationType()).isNotIn("angry", "ANGRY");

        assertThat(savedNodes.get("50:1").getChoiceData()).contains("\"emotion\":\"angry\"");
    }

    @Test
    @DisplayName("이어할 학습이 없으면 found=false — 오류가 아니다")
    void resumeNotFound() {

        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.found()).isFalse();
        assertThat(res.dailyGoal()).isEqualTo(3);
        assertThat(res.node()).isNull();
    }

    @Test
    @DisplayName("그만둔 학습은 마지막으로 본 화면부터 이어진다")
    void resumeFound() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, 1L, req);

        resumable = savedSession;
        answeredStage = "STORY";
        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.found()).isTrue();
        assertThat(res.sessionId()).isEqualTo(100L);
        assertThat(res.storyId()).isEqualTo(50L);
        assertThat(res.resumeScreen()).isEqualTo("feel");
        assertThat(res.node().getStageType()).isEqualTo("MIND");
        assertThat(res.title()).isNotBlank();
    }

    @Test
    @DisplayName("한 문제도 안 답한 세션은 이어하지 않고 새 이야기로 간다")
    void doesNotResumeUntouchedSession() {

        storyService.start(new StoryRequestDTO.Start());
        resumable = savedSession;
        answeredStage = null;
        assertThat(storyService.resume(1L).found()).isFalse();
    }

    @Test
    @DisplayName("이어하기 — 아이가 마음까지 답했으면 그 다음(왜?)부터")
    void resumeFromAnsweredStage() {

        storyService.start(new StoryRequestDTO.Start());
        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, 1L, req);
        req.setStageType("MIND");
        storyService.next(100L, 1L, req);

        resumable = savedSession;
        answeredStage = "MIND";

        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.resumeScreen()).isEqualTo("why");
        assertThat(res.node().getStageType()).isEqualTo("CAUSE");
        assertThat(res.node().getOptions()).isNotEmpty();
    }

    @Test
    @DisplayName("이어서 마쳐도 앞서 저장한 기록은 지워지지 않는다")
    void resumeDoesNotWipeEarlierLogs() {

        storyService.start(new StoryRequestDTO.Start());
        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            storyService.next(100L, 1L, req);
        }

        StoryRequestDTO.Finish quit = new StoryRequestDTO.Finish();
        quit.setCompleted(false);
        quit.setResults(List.of(item(2, "CHOICE", "sad", true)));
        storyService.finish(100L, 1L, quit);

        deletedNodeIds.clear();

        StoryRequestDTO.Finish done = new StoryRequestDTO.Finish();
        done.setCompleted(true);
        done.setResults(List.of(
                item(3, "CHOICE", "cause", true),
                item(4, "EXPRESSION", "sad", true)));
        storyService.finish(100L, 1L, done);

        assertThat(deletedNodeIds).doesNotContain(nodeIdOf(2));
        assertThat(deletedNodeIds).contains(nodeIdOf(3), nodeIdOf(4));
    }

    private Long nodeIdOf(int order) {
        return savedNodes.get("50:" + order).getNodeId();
    }

    @Test
    @DisplayName("남의 아이 세션이면 진행도 종료도 막는다")
    void otherChildSession() {

        startSad();

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        assertThatThrownBy(() -> storyService.next(100L, 2L, req))
                .isInstanceOf(IllegalArgumentException.class);

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(true);

        assertThatThrownBy(() -> storyService.finish(100L, 2L, finish))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> storyService.next(100L, null, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("없는 세션이면 400 으로 떨어지는 예외를 던진다")
    void unknownSession() {

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        assertThatThrownBy(() -> storyService.next(999L, 1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static StoryRequestDTO.Finish.Item item(int order, String type, String value, boolean ok) {
        StoryRequestDTO.Finish.Item i = new StoryRequestDTO.Finish.Item();
        i.setNodeOrder(order);
        i.setMissionType(type);
        i.setResponseValue(value);
        i.setSuccess(ok);
        return i;
    }

    @Test
    @DisplayName("보호자가 감정을 안 골랐으면 이번 주 로드맵 상황에 어울리는 감정을 쓴다")
    void emotionFollowsRoadmapWeek() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(3).stage("또래와 어울리기").topic("친구의 좋은 일 함께 기뻐하기")
                .situationType(SituationType.CELEBRATE.label())
                .goal("친구가 잘했을 때 같이 기뻐해 봐요")
                .build());
        StoryResponseDTO.Start res = storyService.start(new StoryRequestDTO.Start());
        assertThat(res.emotion()).isIn(SituationType.CELEBRATE.emotions());
        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.CELEBRATE.label());
    }

    @Test
    @DisplayName("같은 주차라도 이야기마다 감정이 갈린다")
    void emotionVariesBetweenStories() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        java.util.Set<String> seen = new java.util.HashSet<>();

        java.util.List<String> order = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            storyCount = i;
            String emo = storyService.start(new StoryRequestDTO.Start()).emotion();
            seen.add(emo);
            order.add(emo);
        }

        assertThat(seen).as("네 편이 모두 같은 감정이면 안 된다").hasSizeGreaterThan(1);

        for (int i = 1; i < order.size(); i++) {
            assertThat(order.get(i))
                    .as("%d번째와 %d번째 감정이 연달아 같다", i, i + 1)
                    .isNotEqualTo(order.get(i - 1));
        }
    }

    private String promptSent() {
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(gemini, org.mockito.Mockito.atLeastOnce())
                .generateJson(anyString(), captor.capture());
        return captor.getValue();
    }

    private void withPlannedRoadmap() {

        List<RoadmapPlanDTO.Week> weeks = new ArrayList<>();
        weeks.add(RoadmapPlanDTO.Week.builder().no(1).stage("도움 요청")
                .topic("장난감이 움직이지 않아 당황한 아이가 엄마를 바라보는 장면")
                .situationType(SituationType.ASK_HELP.label()).goal("도와 달라고 해 봐요").build());
        weeks.add(RoadmapPlanDTO.Week.builder().no(2).stage("도움 요청")
                .topic("높은 곳에 있는 과자를 바라보며 아빠를 부르는 장면")
                .situationType(SituationType.ASK_HELP.label()).goal("불러서 알려 봐요").build());
        weeks.add(RoadmapPlanDTO.Week.builder().no(3).stage("차례 지키기")
                .topic("놀이터 미끄럼틀 뒤에서 차례를 기다리는 장면")
                .situationType(SituationType.WAIT_TURN.label()).goal("기다려 봐요").build());

        RoadmapPlanDTO plan = new RoadmapPlanDTO();
        plan.setTotalWeeks(12);
        plan.setWeeks(weeks);

        when(roadmapService.getOrCreate(any())).thenReturn(RoadmapDTO.builder()
                .roadmapId(700L).childId(1L).roadmapType(RoadmapDTO.TYPE_AI).isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .plan(plan)
                .build());

        when(roadmapService.currentWeekPlan(any())).thenReturn(weeks.get(0));
    }

    @Test
    @DisplayName("그 주 첫 이야기는 주차 주제를 그대로 장면으로 만들라고 지시한다")
    void firstStoryOfWeekFollowsTheTopic() {

        withPlannedRoadmap();

        storyService.start(new StoryRequestDTO.Start());

        assertThat(promptSent())
                .contains("[이번 주 첫 이야기인가] 예")
                .contains("장난감이 움직이지 않아 당황한 아이가 엄마를 바라보는 장면");
    }

    @Test
    @DisplayName("앞으로 다룰 주차 주제도 지금 쓰지 말라고 알려 준다")
    void tellsAiWhatIsComingLater() {

        withPlannedRoadmap();
        storyService.start(new StoryRequestDTO.Start());
        String prompt = promptSent();

        assertThat(prompt).contains("[앞으로 다룰 주제 — 지금 쓰면 안 된다]");
        assertThat(prompt).contains("놀이터 미끄럼틀 뒤에서 차례를 기다리는 장면");
    }

    @Test
    @DisplayName("그 주 두 번째 이야기부터는 주제를 그대로 만들지 않는다")
    void laterStoriesOfWeekVary() {

        withPlannedRoadmap();
        when(storyMapperRef.countStoriesSince(any(), any())).thenReturn(1);

        storyService.start(new StoryRequestDTO.Start());
        assertThat(promptSent()).contains("[이번 주 첫 이야기인가] 아니오");
    }

    @Test
    @DisplayName("학습을 시작하면 여섯 화면이 한꺼번에 만들어진다")
    void startBuildsEveryStage() {

        storyService.start(new StoryRequestDTO.Start());

        for (StoryStage stage : StoryStage.values()) {
            assertThat(savedNodes.get(savedStory.getStoryId() + ":" + stage.seq()))
                    .as("%s 화면이 없다", stage)
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("세션 시작 응답의 동작이 동작 화면의 정답값과 같다")
    void startCarriesTheGestureTheActionScreenUses() {

        StoryResponseDTO.Start res = storyService.start(new StoryRequestDTO.Start());

        StoryNodeDTO action = savedNodes.get(savedStory.getStoryId() + ":" + StoryStage.ACTION.seq());

        assertThat(res.gesture())
                .as("상황 카드가 동작을 모르면 동작 화면과 딴소리를 한다")
                .isNotBlank()
                .isEqualTo(action.getTargetValue());
    }
    @Test
    @DisplayName("마음 읽기는 이번 이야기의 상황을 되짚어 준다")
    void mindRecapsThisStory() {

        storyService.start(new StoryRequestDTO.Start());

        StoryNodeDTO mind = savedNodes.get(savedStory.getStoryId() + ":" + StoryStage.MIND.seq());

        assertThat(mind.getNarration())
                .as("앞 화면과 이어지지 않으면 아이가 무엇을 보고 고를지 알 수 없다")
                .contains("장난감");
    }

    @Test
    @DisplayName("미리 만들어 둔 이야기가 있으면 그것을 쓰고 AI 를 다시 부르지 않는다")
    void adoptsPreparedSessionInsteadOfMakingAnother() {
        storyService.start(new StoryRequestDTO.Start());
        StorySessionDTO made = savedSession;
        StoryDTO madeStory = savedStory;

        prepared = StorySessionDTO.builder()
                .sessionId(made.getSessionId())
                .childId(made.getChildId())
                .storyId(madeStory.getStoryId())
                .status(StorySessionDTO.INCOMPLETE)
                .prepared(true)
                .build();

        StoryResponseDTO.Start res = storyService.start(new StoryRequestDTO.Start());

        assertThat(res.sessionId())
                .as("새로 만들지 않고 미리 만들어 둔 세션을 그대로 쓴다")
                .isEqualTo(made.getSessionId());
        assertThat(res.storyId()).isEqualTo(madeStory.getStoryId());
        assertThat(savedStory)
                .as("이야기를 또 만들면 AI 를 두 번 부른 것이다")
                .isSameAs(madeStory);

        assertThat(res.node()).isNotNull();
    }

    @Test
    @DisplayName("오늘의 일상을 적어 줬으면 미리 만들어 둔 이야기를 쓰지 않는다")
    void dailyInputBeatsPreparedStory() {
        storyService.start(new StoryRequestDTO.Start());

        StorySessionDTO made = savedSession;
        prepared = StorySessionDTO.builder()
                .sessionId(made.getSessionId())
                .childId(made.getChildId())
                .storyId(savedStory.getStoryId())
                .status(StorySessionDTO.INCOMPLETE)
                .prepared(true)
                .build();

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setDailyInput("문구점에서 스티커를 샀어요");
        storyService.start(req);

        assertThat(savedSession)
                .as("아껴 둔 편을 그대로 쓰면 세션이 새로 만들어지지 않는다")
                .isNotSameAs(made);

        assertThat(savedSession.getDailyInput())
                .as("적어 준 내용이 이번 편의 재료로 남아야 한다")
                .isEqualTo("문구점에서 스티커를 샀어요");
    }

    @Test
    @DisplayName("직전 편과 같은 감정은 정답으로 내지 않는다")
    void neverRepeatsPreviousEmotion() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        storyCount = 0;
        lastEmotion = null;
        String natural = storyService.start(new StoryRequestDTO.Start()).emotion();

        lastEmotion = natural;
        String next = storyService.start(new StoryRequestDTO.Start()).emotion();
        assertThat(next)
                .as("직전과 같은 감정이 또 정답이 되면 아이가 고르지 않고 외운다")
                .isNotEqualTo(natural);
    }

    @Test
    @DisplayName("이야기를 저장할 때 중심 감정도 함께 남긴다")
    void storyKeepsItsEmotion() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        String emotion = storyService.start(new StoryRequestDTO.Start()).emotion();

        assertThat(savedStory.getEmotion()).isEqualTo(emotion);
    }

    @Test
    @DisplayName("날짜가 바뀌어도 감정이 되돌아가지 않는다")
    void emotionDoesNotResetNextDay() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        storyCount = 2;
        todaySessions = 2;
        String yesterday = storyService.start(new StoryRequestDTO.Start()).emotion();
        storyCount = 3;
        todaySessions = 0;
        String today = storyService.start(new StoryRequestDTO.Start()).emotion();

        assertThat(today).isNotEqualTo(yesterday);
    }

    @Test
    @DisplayName("보호자가 오늘의 기록에서 감정을 골랐으면 그쪽이 이긴다")
    void caregiverEmotionWins() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(3).stage("또래와 어울리기").topic("함께 기뻐하기")
                .situationType(SituationType.CELEBRATE.label())
                .goal("같이 기뻐해 봐요")
                .build());

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("화남");

        assertThat(storyService.start(req).emotion()).isEqualTo("angry");
    }

    @Test
    @DisplayName("로드맵이 없으면 슬픔으로 둔다 — 이야기는 그래도 만들어진다")
    void fallsBackToSadWithoutRoadmap() {
        when(roadmapService.currentWeekPlan(any())).thenReturn(null);

        assertThat(storyService.start(new StoryRequestDTO.Start()).emotion()).isEqualTo("sad");
    }
}
