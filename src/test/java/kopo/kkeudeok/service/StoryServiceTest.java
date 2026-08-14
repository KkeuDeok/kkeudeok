package kopo.kkeudeok.service;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.dto.StoryDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryOptionDTO;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.dto.StorySessionDTO;
import kopo.kkeudeok.mapper.MissionLogMapper;
import kopo.kkeudeok.mapper.StoryMapper;
import kopo.kkeudeok.mapper.StorySessionMapper;
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

// 학습 세션 흐름 검증
class StoryServiceTest {

    private StoryService storyService;

    private final Map<String, StoryNodeDTO> savedNodes = new HashMap<>();

    /** finish 가 실제로 넘긴 결과 행. */
    private final List<MissionLogDTO> savedLogs = new ArrayList<>();

    private StorySessionDTO savedSession;
    private StoryDTO savedStory;

    /** 이어할 세션으로 돌려줄 것. 기본은 없음. */
    private StorySessionDTO resumable;

    /** 아이가 실제로 답한 마지막 단계 — null 이면 아직 아무것도 안 했다는 뜻 */
    private String answeredStage;

    /** deleteByNodes 가 실제로 지우려 한 노드들 — 지우는 범위가 맞는지 본다. */
    private final List<Long> deletedNodeIds = new ArrayList<>();

    @BeforeEach
    void setUp() {

        // --- AI: 키가 없어 꺼진 상태 → 내장 시나리오로 흐른다 ---
        GeminiClient gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(false);
        when(gemini.generateJson(anyString(), anyString())).thenReturn(Optional.empty());

        ObjectMapper objectMapper = new ObjectMapper();
        IStoryAiService aiService = new GeminiStoryAiService(gemini, objectMapper);

        // --- 아이 ---
        IChildService childService = childId -> ChildDTO.builder()
                .childId(1L)
                .name("김지우")
                .birthDate(LocalDate.now().minusYears(6))
                .disorderType("자폐")
                .severity("경도")
                .characterType("tori")
                .build();

        // --- 매퍼 ---
        StoryMapper storyMapper = mock(StoryMapper.class);
        StorySessionMapper sessionMapper = mock(StorySessionMapper.class);
        MissionLogMapper logMapper = mock(MissionLogMapper.class);

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
        when(sessionMapper.countTodaySessions(any())).thenReturn(1);
        when(sessionMapper.countTodayCompleted(any())).thenReturn(1);

        when(storyMapper.selectNodes(any())).thenAnswer(inv -> savedNodes.values().stream()
                .filter(n -> n.getStoryId().equals(inv.getArgument(0)))
                .sorted(java.util.Comparator.comparing(StoryNodeDTO::getNodeOrder))
                .toList());

        when(sessionMapper.selectResumable(any())).thenAnswer(inv -> resumable);
        when(logMapper.selectLastAnsweredStage(any())).thenAnswer(inv -> answeredStage);

        when(logMapper.insertLogs(any())).thenAnswer(inv -> {
            savedLogs.addAll(inv.getArgument(0));
            return savedLogs.size();
        });

        when(logMapper.deleteByNodes(any(), any())).thenAnswer(inv -> {
            deletedNodeIds.addAll(inv.getArgument(1));
            return 0;
        });

        // 로드맵은 정석 커리큘럼 1개를 들고 있는 것으로 세운다 —
        // 이야기 생성이 이번 주 주제를 받아 가는지도 같이 본다.
        IRoadmapService roadmapService = mock(IRoadmapService.class);
        when(roadmapService.getOrCreate(any())).thenReturn(RoadmapDTO.builder()
                .roadmapId(700L)
                .childId(1L)
                .roadmapType(RoadmapDTO.TYPE_STANDARD)
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
                childService, aiService, roadmapService, objectMapper);

        ReflectionTestUtils.setField(storyService, "dailyGoal", 3);
    }

    @Test
    @DisplayName("세션을 시작하면 이야기가 먼저 만들어지고 세션이 그것을 가리킨다")
    void start() {

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("슬픔");                       // 학습 홈이 보내는 한글 그대로
        req.setDailyInput("놀이터에서 친구를 못 만났어요");

        StoryResponseDTO.Start res = storyService.start(req);

        assertThat(res.sessionId()).isEqualTo(100L);
        assertThat(res.storyId()).isEqualTo(50L);
        assertThat(res.emotion()).isEqualTo("sad");   // 한글 → 감정 키로 바뀌었다
        assertThat(res.storySeq()).isEqualTo(1);
        assertThat(res.dailyGoal()).isEqualTo(3);
        assertThat(res.childCallName()).isEqualTo("지우");
        assertThat(res.source()).isEqualTo("FALLBACK");

        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.EXPRESS.label());
        assertThat(savedStory.getChildId()).isEqualTo(1L);
        assertThat(savedStory.getIsGenerated()).isFalse();   // 폴백이라 AI 생성이 아니다

        // 세션은 story 를 가리키고, 아직 미완료다
        assertThat(savedSession.getStoryId()).isEqualTo(50L);
        assertThat(savedSession.getDailyInput()).isEqualTo("놀이터에서 친구를 못 만났어요");
        assertThat(savedSession.getStatus()).isEqualTo("INCOMPLETE");

        StoryNodeDTO node = res.node();
        assertThat(node.getStageType()).isEqualTo("STORY");
        assertThat(node.getNodeOrder()).isEqualTo(1);
        assertThat(node.getMissionType()).isNull();          // 노출만 하는 단계
        assertThat(node.getTitle()).contains("토리");
        assertThat(node.getNarration()).isNotBlank();        // NOT NULL 컬럼이다
        // 씨앗이 첫 노드 JSON 에 실렸는가 — 다음 노드를 이어 쓰는 근거다
        assertThat(node.getChoiceData()).contains("scenario");
    }

    @Test
    @DisplayName("오늘의 일상을 안 넣어도 프로필만으로 이야기가 만들어진다")
    void startWithoutDailyInput() {

        StoryResponseDTO.Start res = storyService.start(new StoryRequestDTO.Start());

        assertThat(res.node().getTitle()).isNotBlank();
        assertThat(res.emotion()).isEqualTo("sad");          // 기본값
        assertThat(savedSession.getDailyInput()).isNull();
    }

    @Test
    @DisplayName("STORY 다음은 MIND — 선택지 3장이 붙고 정답은 하나다")
    void nextToMind() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        StoryResponseDTO.Next res = storyService.next(100L, req);
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

        storyService.start(new StoryRequestDTO.Start());

        StoryResponseDTO.Next res = null;

        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            req.setSuccess(true);
            res = storyService.next(100L, req);
        }

        assertThat(res).isNotNull();
        assertThat(res.node().getStageType()).isEqualTo("PRAISE");
        assertThat(res.node().getNodeOrder()).isEqualTo(6);
        assertThat(res.node().getMissionType()).isNull();
        assertThat(res.last()).isTrue();

        // 칭찬 다음을 또 달라고 하면 막는다
        StoryRequestDTO.Next after = new StoryRequestDTO.Next();
        after.setStageType("PRAISE");
        assertThatThrownBy(() -> storyService.next(100L, after))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("같은 단계를 두 번 요청해도 이야기가 바뀌지 않는다")
    void nextIsIdempotent() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        StoryNodeDTO first = storyService.next(100L, req).node();
        StoryNodeDTO again = storyService.next(100L, req).node();

        assertThat(again.getNodeId()).isEqualTo(first.getNodeId());
        assertThat(again.getTitle()).isEqualTo(first.getTitle());
        // 두 번째는 DB 에서 읽어 온 것 — choice_data 를 풀어 선택지가 살아 있어야 한다
        assertThat(again.getMissionType()).isEqualTo(first.getMissionType());
    }

    @Test
    @DisplayName("세션 종료 — 결과가 노드에 붙고 미션 없는 단계는 버려진다")
    void finish() {

        storyService.start(new StoryRequestDTO.Start());

        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            storyService.next(100L, req);
        }

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(true);
        finish.setResults(List.of(
                item(2, "CHOICE", "angry", false),      // 마음 읽기 — 첫 시도에 틀렸다
                item(3, "VOICE", "넘어져서 아파서", true),  // 이유 찾기 — 말로 답했다
                item(4, "EXPRESSION", "sad", true),
                item(5, "GESTURE", "comfort", true),
                item(1, "CHOICE", "무시됨", true),        // 이야기 단계 — 미션이 없다
                item(99, "CHOICE", "없는 노드", true)     // 없는 순서
        ));

        StoryResponseDTO.Finish res = storyService.finish(100L, finish);

        assertThat(res.status()).isEqualTo("COMPLETED");
        assertThat(res.savedCount()).isEqualTo(4);      // 미션 있는 4건만
        assertThat(res.dailyGoal()).isEqualTo(3);
        assertThat(res.todayDone()).isEqualTo(1);

        assertThat(savedLogs).hasSize(4);

        MissionLogDTO mind = savedLogs.get(0);
        assertThat(mind.getMissionType()).isEqualTo("CHOICE");
        assertThat(mind.getTargetValue()).isEqualTo("sad");     // 노드가 요구한 값
        assertThat(mind.getResponseValue()).isEqualTo("angry"); // 아이가 실제로 한 것
        assertThat(mind.getIsSuccess()).isFalse();
        assertThat(mind.getNodeId()).isNotNull();

        // 이유 찾기는 카드 대신 마이크를 썼다 — 프론트가 알려 준 유형이 저장된다
        assertThat(savedLogs.get(1).getMissionType()).isEqualTo("VOICE");

        // target_value 는 VARCHAR(20), response_value 는 VARCHAR(50) 이다
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
        storyService.next(100L, req);

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(false);
        finish.setResults(List.of(item(2, "CHOICE", "sad", true)));

        StoryResponseDTO.Finish res = storyService.finish(100L, finish);

        assertThat(res.status()).isEqualTo("INCOMPLETE");
        assertThat(res.savedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("긴 음성 문장은 컬럼 길이에 맞춰 잘린다")
    void longVoiceIsTrimmed() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, req);

        StoryRequestDTO.Finish finish = new StoryRequestDTO.Finish();
        finish.setCompleted(true);
        finish.setResults(List.of(item(2, "VOICE", "아".repeat(300), true)));

        storyService.finish(100L, finish);

        assertThat(savedLogs.get(0).getResponseValue()).hasSize(50);
    }

    @Test
    @DisplayName("story.situation_type 에는 감정이 아니라 사회적 상황이 들어간다")
    void situationTypeIsNotEmotion() {

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("화남");

        storyService.start(req);

        // 감정이 화남이어도 상황 분류는 로드맵 주차가 정한다. 'angry' 가 들어가면 안 된다.
        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.EXPRESS.label());
        assertThat(savedStory.getSituationType()).isNotIn("angry", "ANGRY");

        // 감정은 컬럼이 아니라 시나리오 씨앗에 산다
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

        /* 화면이 다음 노드를 미리 받아 뒀다(prefetch) — 아이는 아직 아무것도 답하지 않았다.
           그런데도 마음 화면부터 이어하면 이야기 화면이 통째로 건너뛰어진다(2026-08-14 지적).
           답한 기록이 없으면 처음(이야기)부터가 맞다. */
        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, req);

        resumable = savedSession;      // 매퍼가 이 세션을 이어할 것으로 돌려준다

        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.found()).isTrue();
        assertThat(res.sessionId()).isEqualTo(100L);
        assertThat(res.storyId()).isEqualTo(50L);
        assertThat(res.resumeScreen()).isEqualTo("scene");       // 미리 받아 둔 노드는 진행이 아니다
        assertThat(res.node().getStageType()).isEqualTo("STORY");
        assertThat(res.title()).isNotBlank();
    }

    @Test
    @DisplayName("이어하기 — 아이가 마음까지 답했으면 그 다음(왜?)부터")
    void resumeFromAnsweredStage() {

        storyService.start(new StoryRequestDTO.Start());

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, req);      // MIND 노드 생성
        req.setStageType("MIND");
        storyService.next(100L, req);      // CAUSE 노드 생성(미리 받기)

        resumable = savedSession;
        answeredStage = "MIND";            // 아이가 마음까지 실제로 답했다

        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.resumeScreen()).isEqualTo("why");         // CAUSE 의 화면 키
        assertThat(res.node().getStageType()).isEqualTo("CAUSE");
        assertThat(res.node().getOptions()).isNotEmpty();        // JSON 이 풀렸는가
    }

    @Test
    @DisplayName("이어서 마쳐도 앞서 저장한 기록은 지워지지 않는다")
    void resumeDoesNotWipeEarlierLogs() {

        storyService.start(new StoryRequestDTO.Start());

        for (String stage : new String[]{"STORY", "MIND", "CAUSE", "EXPRESSION", "ACTION"}) {
            StoryRequestDTO.Next req = new StoryRequestDTO.Next();
            req.setStageType(stage);
            storyService.next(100L, req);
        }

        // 1) 마음 읽기까지 하고 그만뒀다 — 그때까지의 결과가 저장된다
        StoryRequestDTO.Finish quit = new StoryRequestDTO.Finish();
        quit.setCompleted(false);
        quit.setResults(List.of(item(2, "CHOICE", "sad", true)));
        storyService.finish(100L, quit);

        // 여기까지 지운 것은 '그때 보낸 노드'뿐이다. 두 번째 전송만 따로 보려고 비운다.
        deletedNodeIds.clear();

        // 2) 나중에 이어서 나머지를 마쳤다 — 앞부분은 다시 보내지 않는다
        StoryRequestDTO.Finish done = new StoryRequestDTO.Finish();
        done.setCompleted(true);
        done.setResults(List.of(
                item(3, "CHOICE", "cause", true),
                item(4, "EXPRESSION", "sad", true)));
        storyService.finish(100L, done);

        // 지운 범위가 '이번에 보낸 노드' 뿐이어야 한다 — 2번 노드는 건드리지 않았다
        assertThat(deletedNodeIds).doesNotContain(nodeIdOf(2));
        assertThat(deletedNodeIds).contains(nodeIdOf(3), nodeIdOf(4));
    }

    private Long nodeIdOf(int order) {
        return savedNodes.get("50:" + order).getNodeId();
    }

    @Test
    @DisplayName("없는 세션이면 400 으로 떨어지는 예외를 던진다")
    void unknownSession() {

        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");

        assertThatThrownBy(() -> storyService.next(999L, req))
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
}
