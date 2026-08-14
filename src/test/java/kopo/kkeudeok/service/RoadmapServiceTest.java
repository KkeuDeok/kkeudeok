package kopo.kkeudeok.service;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.config.GeminiProperties;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.mapper.OnboardingMapper;
import kopo.kkeudeok.mapper.RoadmapMapper;
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

        gemini = mock(GeminiClient.class);
        when(gemini.isEnabled()).thenReturn(false);
        when(gemini.generateJson(anyString(), anyString(), anyInt())).thenReturn(Optional.empty());

        IChildService childService = id -> ChildDTO.builder()
                .childId(1L).name("김지우")
                .birthDate(LocalDate.now().minusYears(6))
                .disorderType("자폐").severity("경도").characterType("tori")
                .build();

        RoadmapMapper mapper = mock(RoadmapMapper.class);

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
        OnboardingMapper onboardingMapper = mock(OnboardingMapper.class);
        when(onboardingMapper.selectChecklist(any())).thenReturn(List.of());

        roadmapService = new RoadmapService(mapper,
                onboardingMapper,
                childService,
                new GeminiRoadmapAiService(gemini, new GeminiProperties(), objectMapper),
                objectMapper);
    }

    @Test
    @DisplayName("로드맵이 없으면 그 자리에서 만들어 준다 — 학습을 막지 않는다")
    void createsWhenMissing() {

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(r.getRoadmapId()).isNotNull();
        assertThat(r.getIsActive()).isTrue();
        // AI 가 꺼져 있으니 정석 커리큘럼이다
        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_STANDARD);

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

        roadmapService.getOrCreate(1L);
        RoadmapDTO next = roadmapService.regenerate(1L);

        assertThat(deactivated).isEqualTo(1);
        assertThat(saved).hasSize(2);
        assertThat(next.getRoadmapId()).isNotEqualTo(saved.get(0).getRoadmapId());
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

    @Test
    @DisplayName("AI 계획이 규칙에 안 맞으면 정석 커리큘럼으로 되돌린다")
    void rejectsBrokenAiPlan() {

        // 주 수 합이 12 가 아닌 계획 — 화면 소제목이 엉뚱한 줄에 붙는다
        when(gemini.isEnabled()).thenReturn(true);
        when(gemini.generateJson(anyString(), anyString(), anyInt())).thenReturn(Optional.of("""
                {"stages":[{"name":"감정 표현","weeks":5}],
                 "weeks":[{"no":1,"stage":"감정 표현","topic":"표정","situationType":"감정 표현하기"}]}
                """));

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_STANDARD);
        assertThat(r.getPlan().getWeeks()).hasSize(12);
    }

    @Test
    @DisplayName("AI 계획이 규칙에 맞으면 그대로 쓰고 AI 로 표시한다")
    void acceptsValidAiPlan() {

        StringBuilder weeks = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            weeks.append(i > 1 ? "," : "")
                    .append("{\"no\":%d,\"stage\":\"연습\",\"topic\":\"%d주 주제\",".formatted(i, i))
                    .append("\"situationType\":\"차례 기다리기\",\"goal\":\"목표\"}");
        }

        when(gemini.isEnabled()).thenReturn(true);
        when(gemini.generateJson(anyString(), anyString(), anyInt())).thenReturn(Optional.of(
                "{\"stages\":[{\"name\":\"연습\",\"weeks\":12}],\"weeks\":[" + weeks + "]}"));

        RoadmapDTO r = roadmapService.getOrCreate(1L);

        assertThat(r.getRoadmapType()).isEqualTo(RoadmapDTO.TYPE_AI);
        assertThat(r.getPlan().getWeeks()).hasSize(12);
        assertThat(r.getPlan().getWeeks().get(0).getTopic()).isEqualTo("1주 주제");
        assertThat(r.getPlan().getWeeks().get(0).getSituationType())
                .isEqualTo(SituationType.WAIT_TURN.label());
    }
}
