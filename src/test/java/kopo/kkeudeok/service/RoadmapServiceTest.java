package kopo.kkeudeok.service;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.config.GeminiProperties;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.mapper.IOnboardingMapper;
import kopo.kkeudeok.mapper.IRoadmapMapper;
import kopo.kkeudeok.service.impl.GeminiRoadmapAiService;
import kopo.kkeudeok.service.impl.RoadmapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 로드맵 생성·주차 계산 검증. */
class RoadmapServiceTest {

    private RoadmapService roadmapService;
    private GeminiClient gemini;

    private final List<RoadmapDTO> saved = new ArrayList<>();
    private RoadmapDTO active;
    private int deactivated;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        /* 정석 커리큘럼을 없앴으므로(2026-08-14) 로드맵은 AI 만이 만든다.
           테스트는 폴백에 기대지 않고 AI 응답을 직접 쥐어 준다 — 무엇을 검증하는지 분명해진다. */
        gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(true);
        /* 로드맵은 자기 모델을 골라서 부른다(4번째 인자) — 이야기와 한도를 나눠 쓰기 위해서다 */
        when(gemini.generateJson(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(Optional.of(validPlanJson()));

        IChildService childService = mock(IChildService.class);
        when(childService.getChild(any())).thenReturn(ChildDTO.builder()
                .childId(1L).name("김지우")
                .birthDate(LocalDate.now().minusYears(6))
                .disorderType("자폐").severity("경도").characterType("tori")
                .build());

        IRoadmapMapper mapper = mock(IRoadmapMapper.class);

        when(mapper.selectActive(any())).thenAnswer(inv -> active);

        when(mapper.insertRoadmap(any())).thenAnswer(inv -> {
            RoadmapDTO r = inv.getArgument(0);
            r.setRoadmapId(100L + saved.size());
            r.setCreatedAt(LocalDateTime.now());
            saved.add(r);
            active = r;
            return 1;
        });

        when(mapper.deactivateAll(any())).thenAnswer(inv -> {
            deactivated++;
            active = null;
            return 1;
        });

        // 체크리스트는 비어 있는 것으로 둔다 — 로드맵은 응답이 없어도 만들어져야 한다
        IOnboardingMapper onboardingMapper = mock(IOnboardingMapper.class);
        when(onboardingMapper.selectChecklist(any())).thenReturn(List.of());

        /* 주차별 완료 표시용 — 여기서는 마친 학습이 없는 것으로 둔다 */
        sessionMapper = mock(kopo.kkeudeok.mapper.IStorySessionMapper.class);
        when(sessionMapper.selectCompletedStartedAt(any())).thenReturn(List.of());

        roadmapService = new RoadmapService(mapper,
                onboardingMapper,
                childService,
                new GeminiRoadmapAiService(gemini, new GeminiProperties(), objectMapper),
                objectMapper,
                sessionMapper);
    }

    private kopo.kkeudeok.mapper.IStorySessionMapper sessionMapper;

    /**
     * 완료 표시는 <b>해낸 것</b>으로 정해야 한다.
     *
     * <p>예전에는 화면이 "지금 주차보다 앞이면 완료" 로 그렸다. 그러면 한 편도 안 했어도
     * 시간이 흐르면 완료가 되고, 이번 주에 다 마쳐도 미완료로 남는다(2026-08-20 지적).
     */
    @Test
    @DisplayName("마친 주차는 학습 기록으로 정한다 — 날짜만으로 완료가 되지 않는다")
    void doneWeeksComeFromRecords() {

        RoadmapDTO roadmap = RoadmapDTO.builder()
                .roadmapId(1L).childId(1L)
                .createdAt(java.time.LocalDateTime.now().minusDays(20))   /* 3주차 진행 중 */
                .plan(planOf(12))
                .build();

        /* 1주차(2일째)와 3주차(15일째)에만 끝까지 마쳤다 — 2주차는 한 편도 못 했다 */
        when(sessionMapper.selectCompletedStartedAt(any())).thenReturn(List.of(
                roadmap.getCreatedAt().plusDays(2),
                roadmap.getCreatedAt().plusDays(15)));

        assertThat(roadmapService.doneWeeks(1L, roadmap))
                .as("날짜가 지났다고 2주차가 완료되면 안 된다")
                .containsExactlyInAnyOrder(1, 3);
    }

    /** 로드맵을 짜기 전에 한 학습은 이 계획의 것이 아니다. */
    @Test
    @DisplayName("로드맵보다 먼저 한 학습은 주차에 넣지 않는다")
    void doneWeeksIgnoreOlderSessions() {

        RoadmapDTO roadmap = RoadmapDTO.builder()
                .roadmapId(1L).childId(1L)
                .createdAt(java.time.LocalDateTime.now().minusDays(3))
                .plan(planOf(12))
                .build();

        when(sessionMapper.selectCompletedStartedAt(any())).thenReturn(List.of(
                roadmap.getCreatedAt().minusDays(5)));

        assertThat(roadmapService.doneWeeks(1L, roadmap)).isEmpty();
    }

    private RoadmapPlanDTO planOf(int weeks) {
        RoadmapPlanDTO plan = new RoadmapPlanDTO();
        plan.setTotalWeeks(weeks);
        return plan;
    }

    /** 규칙에 맞는 12주 계획 JSON — 여러 테스트가 같이 쓴다. */
    private static String validPlanJson() {

        StringBuilder weeks = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            weeks.append(i > 1 ? "," : "")
                    .append("{\"no\":%d,\"stage\":\"연습\",\"topic\":\"%d주 주제\",".formatted(i, i))
                    .append("\"situationType\":\"차례 기다리기\",\"goal\":\"목표\"}");
        }

        return "{\"stages\":[{\"name\":\"연습\",\"weeks\":12}],\"weeks\":[" + weeks + "]}";
    }

    @Test
    @DisplayName("로드맵이 없으면 그 자리에서 만들어 준다 — 학습을 막지 않는다")
    void createsWhenMissing() {

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(r.getRoadmapId()).isNotNull();
        assertThat(r.getIsActive()).isTrue();
        // 로드맵은 AI 만이 만든다
        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_AI);

        RoadmapPlanDTO plan = r.getPlan();
        assertThat(plan.getTotalWeeks()).isEqualTo(12);
        assertThat(plan.getWeeks()).hasSize(12);

        // 단계 주 수의 합이 전체 주차와 맞아야 소제목이 제 줄에 붙는다
        assertThat(plan.getStages().stream().mapToInt(RoadmapPlanDTO.Stage::getWeeks).sum())
                .isEqualTo(12);

        // 주차마다 사회적 상황이 고정 목록 안에 있어야 이야기 생성으로 이어진다
        List<String> allowed = java.util.Arrays.stream(SituationType.values())
                .map(SituationType::label).toList();
        assertThat(plan.getWeeks()).allSatisfy(w -> {
            assertThat(w.getSituationType()).isIn(allowed);
            assertThat(w.getTopic()).isNotBlank();
            assertThat(w.getStage()).isNotBlank();
            assertThat(w.getGoal()).isNotBlank();
        });

        // step_data 는 NOT NULL + json_valid 다
        assertThat(r.getStepData()).isNotBlank().contains("weeks");
    }

    @Test
    @DisplayName("이미 있으면 새로 만들지 않는다")
    void reusesActive() {

        RoadmapDTO first = roadmapService.getOrCreate(1L);
        RoadmapDTO again = roadmapService.getOrCreate(1L);

        assertThat(again.getRoadmapId()).isEqualTo(first.getRoadmapId());
        assertThat(saved).hasSize(1);
    }

    @Test
    @DisplayName("다시 추천받으면 쓰던 것을 내리고 새로 만든다 — 지우지는 않는다")
    void regenerate() {

        RoadmapDTO first = roadmapService.getOrCreate(1L);
        RoadmapDTO next = roadmapService.regenerate(1L);

        /* 호출 횟수가 아니라 결과를 본다 — 넣기 직전에도 한 번 내리므로(활성은 늘 하나여야 한다)
           deactivateAll 이 몇 번 불렸는지는 구현에 따라 달라진다. 중요한 건 '내려갔다' 는 사실이다. */
        assertThat(deactivated).isPositive();
        assertThat(saved).hasSize(2);
        assertThat(next.getRoadmapId()).isNotEqualTo(first.getRoadmapId());

        // 지우지는 않는다 — 지난 로드맵은 기록으로 남는다
        assertThat(saved.get(0).getRoadmapId()).isEqualTo(first.getRoadmapId());
    }

    @Test
    @DisplayName("주차는 만든 날로부터 센다 — 12주를 넘겨도 마지막 주에 머문다")
    void currentWeek() {

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(roadmapService.currentWeek(r)).isEqualTo(1);

        r.setCreatedAt(LocalDateTime.now().minusDays(15));       // 3주차
        assertThat(roadmapService.currentWeek(r)).isEqualTo(3);

        r.setCreatedAt(LocalDateTime.now().minusDays(365));      // 한참 지났다
        assertThat(roadmapService.currentWeek(r)).isEqualTo(12); // 마지막 주에 머문다

        assertThat(roadmapService.currentWeekPlan(r).getNo()).isEqualTo(12);
    }

    /**
     * 정석 커리큘럼을 없앤 뒤로는 '되돌릴 곳' 이 없다.
     * 규칙에 안 맞는 계획을 저장해 버리면 로드맵이 있는 것으로 읽혀 [학습 시작하기] 가 열리고,
     * 그 아이에게 맞지 않는 이야기가 만들어진다. 없는 채로 두어야 다시 짠다.
     */
    @Test
    @DisplayName("AI 계획이 규칙에 안 맞으면 아무것도 저장하지 않는다")
    void rejectsBrokenAiPlan() {

        // 주 수 합이 12 가 아닌 계획 — 화면 소제목이 엉뚱한 줄에 붙는다
        when(gemini.generateJson(anyString(), anyString(), anyInt(), anyString())).thenReturn(Optional.of("""
                {"stages":[{"name":"감정 표현","weeks":5}],
                 "weeks":[{"no":1,"stage":"감정 표현","topic":"표정","situationType":"감정 표현하기"}]}
                """));

        assertThat(roadmapService.getOrCreate(1L)).isNull();
        assertThat(saved).isEmpty();
    }

    @Test
    @DisplayName("AI 를 못 쓰면 로드맵을 만들지 않는다 — 화면이 [학습 시작하기] 를 잠근다")
    void noRoadmapWhenAiOff() {

        when(gemini.isEnabled()).thenReturn(false);

        assertThat(roadmapService.getOrCreate(1L)).isNull();
        assertThat(saved).isEmpty();
    }

    @Test
    @DisplayName("AI 계획이 규칙에 맞으면 그대로 쓰고 AI 로 표시한다")
    void acceptsValidAiPlan() {

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_AI);
        assertThat(r.getPlan().getWeeks()).hasSize(12);
        assertThat(r.getPlan().getWeeks().get(0).getTopic()).isEqualTo("1주 주제");
        assertThat(r.getPlan().getWeeks().get(0).getSituationType())
                .isEqualTo(SituationType.WAIT_TURN.label());
    }

    /**
     * 만드는 길이 둘(getOrCreate·regenerate)이라 겹치면 활성 로드맵이 두 개가 됐다
     * (2026-08-15 실제 확인). 겹쳐 부르면 한 번만 만들어야 한다.
     */
    @Test
    @DisplayName("동시에 불러도 로드맵은 한 번만 만든다 — 활성이 둘이 되지 않는다")
    void doesNotCreateTwice() throws Exception {

        Thread a = new Thread(() -> roadmapService.getOrCreate(1L));
        Thread b = new Thread(() -> roadmapService.getOrCreate(1L));

        a.start(); b.start();
        a.join(); b.join();

        assertThat(saved).hasSize(1);
    }
}
