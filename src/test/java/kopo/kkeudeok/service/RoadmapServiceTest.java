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

class RoadmapServiceTest {

    private RoadmapService roadmapService;
    private GeminiClient gemini;

    private final List<RoadmapDTO> saved = new ArrayList<>();
    private RoadmapDTO active;
    private int deactivated;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(true);
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

        IOnboardingMapper onboardingMapper = mock(IOnboardingMapper.class);
        when(onboardingMapper.selectChecklist(any())).thenReturn(List.of());

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

    @Test
    @DisplayName("마친 주차는 학습 기록으로 정한다 — 날짜만으로 완료가 되지 않는다")
    void doneWeeksComeFromRecords() {
        RoadmapDTO roadmap = RoadmapDTO.builder()
                .roadmapId(1L).childId(1L)
                .createdAt(java.time.LocalDateTime.now().minusDays(20))
                .plan(planOf(12))
                .build();

        when(sessionMapper.selectCompletedStartedAt(any())).thenReturn(List.of(
                roadmap.getCreatedAt().plusDays(2),
                roadmap.getCreatedAt().plusDays(15)));

        assertThat(roadmapService.doneWeeks(1L, roadmap))
                .as("날짜가 지났다고 2주차가 완료되면 안 된다")
                .containsExactlyInAnyOrder(1, 3);
    }

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
        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_AI);

        RoadmapPlanDTO plan = r.getPlan();
        assertThat(plan.getTotalWeeks()).isEqualTo(12);
        assertThat(plan.getWeeks()).hasSize(12);

        assertThat(plan.getStages().stream().mapToInt(RoadmapPlanDTO.Stage::getWeeks).sum())
                .isEqualTo(12);

        List<String> allowed = java.util.Arrays.stream(SituationType.values())
                .map(SituationType::label).toList();
        assertThat(plan.getWeeks()).allSatisfy(w -> {
            assertThat(w.getSituationType()).isIn(allowed);
            assertThat(w.getTopic()).isNotBlank();
            assertThat(w.getStage()).isNotBlank();
            assertThat(w.getGoal()).isNotBlank();
        });

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

        assertThat(deactivated).isPositive();
        assertThat(saved).hasSize(2);
        assertThat(next.getRoadmapId()).isNotEqualTo(first.getRoadmapId());

        assertThat(saved.get(0).getRoadmapId()).isEqualTo(first.getRoadmapId());
    }

    @Test
    @DisplayName("주차는 만든 날로부터 센다 — 12주를 넘겨도 마지막 주에 머문다")
    void currentWeek() {
        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(roadmapService.currentWeek(r)).isEqualTo(1);

        r.setCreatedAt(LocalDateTime.now().minusDays(15));
        assertThat(roadmapService.currentWeek(r)).isEqualTo(3);

        r.setCreatedAt(LocalDateTime.now().minusDays(365));
        assertThat(roadmapService.currentWeek(r)).isEqualTo(12);

        assertThat(roadmapService.currentWeekPlan(r).getNo()).isEqualTo(12);
    }

    @Test
    @DisplayName("AI 계획이 규칙에 안 맞으면 아무것도 저장하지 않는다")
    void rejectsBrokenAiPlan() {

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