package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.ReportDTO;
import kopo.kkeudeok.mapper.IMissionLogMapper;
import kopo.kkeudeok.mapper.IOnboardingMapper;
import kopo.kkeudeok.service.impl.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {
    private ReportService reportService;
    private final List<MissionLogDTO> logs = new ArrayList<>();
    private final List<ChecklistAnswerDTO> checklist = new ArrayList<>();

    @BeforeEach
    void setUp() {

        IMissionLogMapper logMapper = mock(IMissionLogMapper.class);
        when(logMapper.selectForReport(any(), any())).thenAnswer(inv -> logs);

        IOnboardingMapper onboardingMapper = mock(IOnboardingMapper.class);
        when(onboardingMapper.selectChecklist(any())).thenAnswer(inv -> checklist);

        IRoadmapService roadmapService = mock(IRoadmapService.class);
        when(roadmapService.currentWeek(any())).thenReturn(3);

        reportService = new ReportService(logMapper, onboardingMapper, roadmapService);
    }

    private void log(String type, String stage, String target, boolean ok) {
        log(type, stage, target, ok, LocalDateTime.now().minusDays(1));
    }

    private void log(String type, String stage, String target, boolean ok, LocalDateTime at) {
        MissionLogDTO l = new MissionLogDTO();
        l.setMissionType(type);
        l.setStageType(stage);
        l.setTargetValue(target);
        l.setIsSuccess(ok);
        l.setStartedAt(at);
        logs.add(l);
    }

    private void answer(String domain, int score) {
        checklist.add(ChecklistAnswerDTO.builder().domain(domain).questionNo(1).score(score).build());
    }

    @Test
    @DisplayName("감정 이해는 마음·왜? 단계의 고르기 성공률이다")
    void understandIsChoiceSuccessRate() {

        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "happy", true);
        log("CHOICE", "CAUSE", "sad", false);
        log("CHOICE", "MIND", "angry", true);

        log("EXPRESSION", "EXPRESSION", "sad", false);

        ReportDTO r = reportService.of(1L);
        assertThat(r.understand().total()).isEqualTo(4);
        assertThat(r.understand().success()).isEqualTo(3);
        assertThat(r.understand().score()).isEqualTo(75);
    }

    @Test
    @DisplayName("감정 표현은 표정·동작·음성 성공률이다")
    void expressIsBodyMissionRate() {

        log("EXPRESSION", "EXPRESSION", "sad", true);
        log("GESTURE", "ACTION", "comfort", false);
        log("VOICE", "CAUSE", "cause", true);
        log("CHOICE", "MIND", "sad", true);

        ReportDTO r = reportService.of(1L);

        assertThat(r.express().total()).isEqualTo(3);
        assertThat(r.express().score()).isEqualTo(67);
    }

    @Test
    @DisplayName("지난주와 견주어 증감을 낸다")
    void deltaComparesWithLastWeek() {

        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);

        LocalDateTime old = LocalDateTime.now().minusDays(10);
        log("CHOICE", "MIND", "sad", true, old);
        log("CHOICE", "MIND", "sad", false, old);
        assertThat(reportService.of(1L).understand().delta()).isEqualTo(50);
    }

    @Test
    @DisplayName("사회성은 체크리스트를 뒤집어 지수로 만든다 (높은 응답 = 낮은 지수)")
    void socialInvertsChecklist() {

        answer("사회적 상호작용", 7);
        answer("감정 표현", 7);
        answer("감정 이해", 7);
        assertThat(reportService.of(1L).social().score()).isZero();

        checklist.clear();
        answer("사회적 상호작용", 1);
        answer("감정 표현", 1);
        answer("감정 이해", 1);

        assertThat(reportService.of(1L).social().score()).isEqualTo(100);
    }

    @Test
    @DisplayName("미션이 몇 번 안 되면 체크리스트 쪽에 무게가 실린다")
    void socialLeansOnChecklistWhenFewMissions() {

        answer("사회적 상호작용", 4);
        answer("감정 표현", 4);
        answer("감정 이해", 4);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", false);
        assertThat(reportService.of(1L).social().score()).isEqualTo(54);
    }

    @Test
    @DisplayName("미션이 쌓일수록 실제 수행이 지수를 끌고 간다")
    void socialShiftsToMissionsAsRecordsGrow() {

        answer("사회적 상호작용", 4);
        answer("감정 표현", 4);
        answer("감정 이해", 4);

        for (int i = 0; i < 60; i++) {
            log("CHOICE", "MIND", "sad", i % 4 != 3);
        }

        assertThat(reportService.of(1L).social().score())
                .as("4회일 때(54)보다 확실히 높아야 한다 — 같은 성공률이지만 근거가 두텁다")
                .isEqualTo(69);
    }

    @Test
    @DisplayName("감정별 이해도는 잘하는 감정이 위로 온다")
    void emotionRatesSortedByRate() {

        log("CHOICE", "MIND", "happy", true);
        log("CHOICE", "MIND", "happy", true);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", false);

        List<ReportDTO.EmotionRate> rows = reportService.of(1L).emotions();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).key()).isEqualTo("happy");
        assertThat(rows.get(0).rate()).isEqualTo(100);
        assertThat(rows.get(1).rate()).isEqualTo(50);
    }

    @Test
    @DisplayName("기록이 없어도 사회성은 0 이 아니다 — 체크리스트가 근거다")
    void emptyIsNotZero() {

        ReportDTO r = reportService.of(1L);
        assertThat(r.hasData()).isFalse();
        assertThat(r.understand().basis()).isEmpty();
        assertThat(r.social().score()).isEqualTo(50);
    }

    @Test
    @DisplayName("학습 전에는 감정 이해·표현도 제 영역 체크리스트를 출발점으로 쓴다")
    void beforeLearningEachMetricUsesItsOwnChecklist() {

        answer("감정 이해", 1);
        answer("감정 표현", 7);
        answer("사회적 상호작용", 4);

        ReportDTO r = reportService.of(1L);

        assertThat(r.understand().score()).isEqualTo(100);
        assertThat(r.express().score()).isZero();

        checklist.clear();
        answer("사회적 상호작용", 4);
        ReportDTO only = reportService.of(1L);
        assertThat(only.understand().score()).isEqualTo(50);
        assertThat(only.express().score()).isEqualTo(50);
    }
    @Test
    @DisplayName("기록이 생기면 체크리스트를 섞지 않고 미션 성공률을 그대로 쓴다")
    void afterLearningUsesMissionRateOnly() {

        answer("감정 이해", 7);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "happy", true);

        assertThat(reportService.of(1L).understand().score())
                .as("체크리스트가 섞이면 100 이 아니라 50 이 된다")
                .isEqualTo(100);
    }

    @Test
    @DisplayName("레이더 축은 순서가 고정이고, 응답이 없는 영역은 가운데(50)로 둔다")
    void radarAxesAreFixedAndOrdered() {

        answer("감정 이해", 1);
        answer("사회적 상호작용", 7);

        List<ReportDTO.DomainIndex> axes = reportService.of(1L).domains();
        assertThat(axes).extracting(ReportDTO.DomainIndex::label)
                .containsExactly("감정 이해", "감정 표현", "사회적 상호작용");

        assertThat(axes.get(0).score()).isEqualTo(100);
        assertThat(axes.get(1).score()).isEqualTo(50);
        assertThat(axes.get(2).score()).isZero();
    }

    @Test
    @DisplayName("감정 조절은 응답이 있어도 레이더 축에 넣지 않는다")
    void radarLeavesOutRegulation() {

        answer("감정 이해", 3);
        answer("감정 표현", 3);
        answer("감정 조절", 3);
        answer("사회적 상호작용", 3);
        assertThat(reportService.of(1L).domains())
                .extracting(ReportDTO.DomainIndex::label)
                .doesNotContain("감정 조절");
    }
}
