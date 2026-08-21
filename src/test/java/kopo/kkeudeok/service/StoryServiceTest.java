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
    private StorySessionDTO prepared;    /* 미리 만들어 둔, 아직 아무도 쓰지 않은 세션 */

    /** 아이가 실제로 답한 마지막 단계 — null 이면 아직 아무것도 안 했다는 뜻 */
    private String answeredStage;

    /** deleteByNodes 가 실제로 지우려 한 노드들 — 지우는 범위가 맞는지 본다. */
    private final List<Long> deletedNodeIds = new ArrayList<>();

    /** 테스트마다 이번 주 계획을 바꿔 끼울 수 있게 필드로 둔다. */
    private IRoadmapService roadmapService;

    /** 감정을 슬픔으로 고정해 시작한다 — 노드 생성을 보는 테스트가 감정 회전에 흔들리지 않게. */
    private StoryResponseDTO.Start startSad() {
        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setEmotion("슬픔");
        return storyService.start(req);
    }

    /** 오늘 몇 번째 이야기인지 — 감정이 이야기마다 갈리는지 보려고 테스트에서 바꾼다. */
    private int todaySessions = 1;
    private int storyCount = 0;      /* 지금까지 만든 이야기 수 — 감정 순번의 근거 */
    private String lastEmotion;      /* 직전 편의 중심 감정 — 연달아 같은 답을 막는 근거 */

    /* 프롬프트에 무엇이 실려 갔는지, 스텁을 바꿔 끼우려고 잡아 둔다 */
    private IStoryMapper storyMapperRef;
    private GeminiClient gemini;

    @BeforeEach
    void setUp() {

        /* AI: 제대로 된 시나리오를 준다.
           정석 시나리오를 없앴으므로(2026-08-14) 이야기는 AI 만이 만든다 — 못 만들면
           StoryService.start 가 끊는다. 그래서 테스트가 AI 응답을 직접 쥐어 준다.
           노드 단계(MIND·CAUSE…)에서도 같은 JSON 이 돌아오지만, 그쪽 스키마와 안 맞으면
           StoryTemplate 이 시나리오를 렌더링한 값이 그대로 남는다 — 그게 원래 동작이다. */
        gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(true);
        when(gemini.generateJson(anyString(), anyString())).thenReturn(Optional.of("""
                {"title":"토리가 슬퍼요","emotion":"sad","situationType":"감정 표현하기",
                 "situation":"토리가 장난감을 잃어버렸어요","cause":"장난감을 잃어버려서",
                 "causeDistractor":"졸려서","gesture":"comfort","praise":"고마워, 마음이 나아졌어"}
                """));

        ObjectMapper objectMapper = new ObjectMapper();

        /* 화면 문구 다듬기는 꺼 둔 채로 본다 — 실제 기본값이 그렇고, 그래야 한 편에
           AI 를 한 번만 부른다(무료 등급 분당 20회). */
        kopo.kkeudeok.config.GeminiProperties props = new kopo.kkeudeok.config.GeminiProperties();

        IStoryAiService aiService = new GeminiStoryAiService(gemini, props, objectMapper);

        // --- 아이 ---
        IChildService childService = mock(IChildService.class);
        when(childService.getChild(any())).thenReturn(ChildDTO.builder()
                .childId(1L)
                .name("김지우")
                .birthDate(LocalDate.now().minusYears(6))
                .disorderType("자폐")
                .severity("경도")
                .characterType("tori")
                .build());

        // --- 매퍼 ---
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

        // 로드맵은 정석 커리큘럼 1개를 들고 있는 것으로 세운다 —
        // 이야기 생성이 이번 주 주제를 받아 가는지도 같이 본다.
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

        /* 미리 만들기 이벤트는 테스트에서 받는 쪽이 없다 — 그냥 흘려보낸다 */
        storyService = new StoryService(storyMapper, sessionMapper, logMapper,
                childService, aiService, roadmapService, objectMapper, event -> { });

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
        assertThat(res.source()).isEqualTo("AI");

        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.EXPRESS.label());
        assertThat(savedStory.getChildId()).isEqualTo(1L);
        assertThat(savedStory.getIsGenerated()).isTrue();    // 이야기는 AI 만이 만든다

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

        startSad();

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

        startSad();

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

        startSad();

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

        /* 화면이 다음 노드를 미리 받아 뒀다(prefetch). 미리 받은 것은 '진행' 이 아니므로
           이어할 자리는 아이가 답한 데까지다 — 마음까지 답했으면 이야기가 아니라 마음부터.
           (2026-08-14: 미리 받은 노드를 진행으로 세어 이야기 화면이 통째로 건너뛰어졌다) */
        StoryRequestDTO.Next req = new StoryRequestDTO.Next();
        req.setStageType("STORY");
        storyService.next(100L, req);

        resumable = savedSession;      // 매퍼가 이 세션을 이어할 것으로 돌려준다
        answeredStage = "STORY";       // 이야기까지는 아이가 실제로 지나왔다

        StoryResponseDTO.Resume res = storyService.resume(1L);

        assertThat(res.found()).isTrue();
        assertThat(res.sessionId()).isEqualTo(100L);
        assertThat(res.storyId()).isEqualTo(50L);
        assertThat(res.resumeScreen()).isEqualTo("feel");        // 이야기 다음은 마음
        assertThat(res.node().getStageType()).isEqualTo("MIND");
        assertThat(res.title()).isNotBlank();
    }

    /**
     * 화면만 열어 보고 나간 세션까지 이어받으면, 다시 시작할 때마다 같은 이야기가 돌아온다
     * (2026-08-18 지적: "학습할 때마다 중복된 스토리"). 답한 것이 없으면 새로 시작한다.
     */
    @Test
    @DisplayName("한 문제도 안 답한 세션은 이어하지 않고 새 이야기로 간다")
    void doesNotResumeUntouchedSession() {

        storyService.start(new StoryRequestDTO.Start());
        resumable = savedSession;
        answeredStage = null;                      // 아무것도 답하지 않았다

        assertThat(storyService.resume(1L).found()).isFalse();
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

    /* ================================================================
     * 로드맵 → 이야기 연결
     * 로드맵이 상황을 정해 놓고 감정은 sad 로 굳어 있으면 AI 가 서로 안 맞는 지시를 받아
     * 로드맵과 겉도는 이야기가 나온다(2026-08-15 지적). 둘 다 로드맵을 따라야 한다.
     * ================================================================ */

    @Test
    @DisplayName("보호자가 감정을 안 골랐으면 이번 주 로드맵 상황에 어울리는 감정을 쓴다")
    void emotionFollowsRoadmapWeek() {

        // '함께 기뻐하기' 주차라면 슬픈 이야기가 나오면 안 된다
        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(3).stage("또래와 어울리기").topic("친구의 좋은 일 함께 기뻐하기")
                .situationType(SituationType.CELEBRATE.label())
                .goal("친구가 잘했을 때 같이 기뻐해 봐요")
                .build());

        StoryResponseDTO.Start res = storyService.start(new StoryRequestDTO.Start());

        assertThat(res.emotion()).isIn(SituationType.CELEBRATE.emotions());
        assertThat(savedStory.getSituationType()).isEqualTo(SituationType.CELEBRATE.label());
    }

    /**
     * 주차는 한 주 내내 그대로다. 감정을 주차 하나에 못 박으면 그 주 이야기의 정답이
     * 전부 같아져 아이가 고르지 않고 외운다(2026-08-15 지적).
     *
     * <p>순번은 <b>누적 이야기 수</b>다. '오늘 몇 번째' 로 세면 날이 바뀔 때 0 으로 돌아가
     * 어제 첫 편과 오늘 첫 편이 같은 감정이 된다(2026-08-18 지적).
     */
    @Test
    @DisplayName("같은 주차라도 이야기마다 감정이 갈린다")
    void emotionVariesBetweenStories() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())   // 네 감정을 다 돌리는 상황
                .goal("마음을 표현해 봐요")
                .build());

        java.util.Set<String> seen = new java.util.HashSet<>();

        java.util.List<String> order = new java.util.ArrayList<>();

        for (int i = 0; i < 4; i++) {
            storyCount = i;                          // 지금까지 i 편을 받았다
            String emo = storyService.start(new StoryRequestDTO.Start()).emotion();
            seen.add(emo);
            order.add(emo);
        }

        assertThat(seen).as("네 편이 모두 같은 감정이면 안 된다").hasSizeGreaterThan(1);

        /* 바로 앞 편과 같은 감정이 이어지면 안 된다 — 슬픔만 계속 나온다는 지적의 핵심이다 */
        for (int i = 1; i < order.size(); i++) {
            assertThat(order.get(i))
                    .as("%d번째와 %d번째 감정이 연달아 같다", i, i + 1)
                    .isNotEqualTo(order.get(i - 1));
        }
    }

    /** AI 에게 실제로 보낸 지시문을 꺼내 온다 — 프롬프트에만 있는 규칙을 확인할 길이다. */
    private String promptSent() {
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(gemini, org.mockito.Mockito.atLeastOnce())
                .generateJson(anyString(), captor.capture());
        return captor.getValue();
    }

    /** 12주 계획을 든 로드맵으로 갈아 끼운다 — 주차 주제·앞으로 다룰 주제를 보려면 필요하다. */
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

    /**
     * 보호자는 로드맵을 미리 읽고 "1주차엔 이런 걸 하는구나" 를 알고 있다.
     * 그 주 첫 이야기가 딴 장면이면 계획이 장식이 된다.
     */
    @Test
    @DisplayName("그 주 첫 이야기는 주차 주제를 그대로 장면으로 만들라고 지시한다")
    void firstStoryOfWeekFollowsTheTopic() {

        withPlannedRoadmap();
        /* countStoriesSince 는 스텁이 없어 0 — 이번 주에 아직 만든 이야기가 없다 */

        storyService.start(new StoryRequestDTO.Start());

        assertThat(promptSent())
                .contains("[이번 주 첫 이야기인가] 예")
                .contains("장난감이 움직이지 않아 당황한 아이가 엄마를 바라보는 장면");
    }

    /**
     * ⚠ 지나간 제목만 피하게 하면 3주차 장면을 1주차에 만들어 버린다 —
     * 정작 그 주가 왔을 때 "저번에 했는데" 가 된다(2026-08-20 요청).
     */
    @Test
    @DisplayName("앞으로 다룰 주차 주제도 지금 쓰지 말라고 알려 준다")
    void tellsAiWhatIsComingLater() {

        withPlannedRoadmap();

        storyService.start(new StoryRequestDTO.Start());

        String prompt = promptSent();

        assertThat(prompt).contains("[앞으로 다룰 주제 — 지금 쓰면 안 된다]");
        assertThat(prompt).contains("놀이터 미끄럼틀 뒤에서 차례를 기다리는 장면");
    }

    /** 그 주에 이미 한 편을 했으면 주제를 되풀이하지 않는다 — 두 번째부터는 다른 장면이다. */
    @Test
    @DisplayName("그 주 두 번째 이야기부터는 주제를 그대로 만들지 않는다")
    void laterStoriesOfWeekVary() {

        withPlannedRoadmap();
        when(storyMapperRef.countStoriesSince(any(), any())).thenReturn(1);

        storyService.start(new StoryRequestDTO.Start());

        assertThat(promptSent()).contains("[이번 주 첫 이야기인가] 아니오");
    }

    /**
     * 한 편은 시작할 때 <b>여섯 화면이 다 만들어져야</b> 한다.
     *
     * <p>화면마다 그때그때 만들면 앞뒤가 안 이어진다 — 도서관 이야기를 보고 왔는데
     * 마음 읽기가 블록 이야기를 되짚어 주는 일이 실제로 있었다(2026-08-19 지적).
     * 뼈대 하나로 여섯 화면이 다 나오므로 미룰 이유가 없다.
     */
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

    /**
     * 마음 읽기의 되짚기는 <b>이번 이야기</b>에서 나와야 한다.
     * 감정별 고정 문장을 쓰면 앞 화면과 딴 이야기가 된다.
     */
    @Test
    @DisplayName("마음 읽기는 이번 이야기의 상황을 되짚어 준다")
    void mindRecapsThisStory() {

        storyService.start(new StoryRequestDTO.Start());

        StoryNodeDTO mind = savedNodes.get(savedStory.getStoryId() + ":" + StoryStage.MIND.seq());

        assertThat(mind.getNarration())
                .as("앞 화면과 이어지지 않으면 아이가 무엇을 보고 고를지 알 수 없다")
                .contains("장난감");                 /* 스텁 시나리오: "토리가 장난감을 잃어버렸어요" */
    }

    /**
     * 미리 만들어 둔 이야기는 <b>버리지 않고 쓴다</b>.
     *
     * <p>예전에는 [학습 시작하기] 가 늘 새로 만들었다. 그래서 온보딩 뒤 미리 만든 한 편은
     * 한 번도 쓰이지 않고 남았다가 '답한 기록 없음' 으로 닫혔다 — AI 를 두 번 부르고
     * 결과는 한 편뿐이다(2026-08-19 로그: 10초 사이 같은 감정의 이야기 두 편).
     * 무료 등급은 분당 20회라 이 낭비가 곧 한도 초과가 된다.
     */
    @Test
    @DisplayName("미리 만들어 둔 이야기가 있으면 그것을 쓰고 AI 를 다시 부르지 않는다")
    void adoptsPreparedSessionInsteadOfMakingAnother() {

        /* 미리 만들어 둔 한 편이 이미 있다 — 이야기와 첫 화면까지 갖춰져 있다 */
        storyService.start(new StoryRequestDTO.Start());          // 이게 그 '미리 만든' 편이다

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

    /**
     * 순번만으로는 부족하다 — 주차가 넘어가 상황 분류가 바뀌면 감정 목록도 바뀌어
     * 순번이 같은 감정에 다시 떨어질 수 있다. 직전 편의 감정을 실제로 보고 피해야 한다.
     */
    @Test
    @DisplayName("직전 편과 같은 감정은 정답으로 내지 않는다")
    void neverRepeatsPreviousEmotion() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        /* 순번이 가리키는 감정을 먼저 알아낸 뒤, 그것이 직전 감정이었다고 알려 준다 */
        storyCount = 0;
        lastEmotion = null;
        String natural = storyService.start(new StoryRequestDTO.Start()).emotion();

        lastEmotion = natural;
        String next = storyService.start(new StoryRequestDTO.Start()).emotion();

        assertThat(next)
                .as("직전과 같은 감정이 또 정답이 되면 아이가 고르지 않고 외운다")
                .isNotEqualTo(natural);
    }

    /** 이야기에 중심 감정을 남겨 둬야 다음 편이 그것을 피할 수 있다. */
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

    /** 날이 바뀌어도(오늘 첫 편이어도) 어제 마지막과 감정이 겹치지 않는다. */
    @Test
    @DisplayName("날짜가 바뀌어도 감정이 되돌아가지 않는다")
    void emotionDoesNotResetNextDay() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(RoadmapPlanDTO.Week.builder()
                .no(1).stage("감정 표현").topic("마음 나타내기")
                .situationType(SituationType.EXPRESS.label())
                .goal("마음을 표현해 봐요")
                .build());

        storyCount = 2;
        todaySessions = 2;                           // 어제의 마지막 편
        String yesterday = storyService.start(new StoryRequestDTO.Start()).emotion();

        storyCount = 3;
        todaySessions = 0;                           // 오늘 첫 편 — 예전에는 여기서 0 으로 돌아갔다
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
        req.setEmotion("화남");                       // 오늘 아이가 화가 났다고 적었다

        assertThat(storyService.start(req).emotion()).isEqualTo("angry");
    }

    @Test
    @DisplayName("로드맵이 없으면 슬픔으로 둔다 — 이야기는 그래도 만들어진다")
    void fallsBackToSadWithoutRoadmap() {

        when(roadmapService.currentWeekPlan(any())).thenReturn(null);

        assertThat(storyService.start(new StoryRequestDTO.Start()).emotion()).isEqualTo("sad");
    }

}
