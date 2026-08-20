package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.ReportDTO;
import kopo.kkeudeok.mapper.MissionLogMapper;
import kopo.kkeudeok.mapper.OnboardingMapper;
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

/**
 * 성장 리포트 산정 검증.
 *
 * <p>수치는 보호자가 아이의 변화를 판단하는 근거다. 계산이 조금만 어긋나도
 * "나아지고 있다/아니다" 를 거꾸로 알려 주게 되므로 자리마다 못 박아 둔다.
 */
class ReportServiceTest {

    private ReportService reportService;

    private final List<MissionLogDTO> logs = new ArrayList<>();
    private final List<ChecklistAnswerDTO> checklist = new ArrayList<>();

    @BeforeEach
    void setUp() {

        MissionLogMapper logMapper = mock(MissionLogMapper.class);
        when(logMapper.selectForReport(any(), any())).thenAnswer(inv -> logs);

        OnboardingMapper onboardingMapper = mock(OnboardingMapper.class);
        when(onboardingMapper.selectChecklist(any())).thenAnswer(inv -> checklist);

        IRoadmapService roadmapService = mock(IRoadmapService.class);
        when(roadmapService.currentWeek(any())).thenReturn(3);

        reportService = new ReportService(logMapper, onboardingMapper, roadmapService);
    }

    /** 이번 주(오늘) 기록 한 줄. */
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
        log("CHOICE", "MIND", "angry", true);          // 4회 중 3회 = 75%

        log("EXPRESSION", "EXPRESSION", "sad", false); // 표현 미션은 여기에 안 섞인다

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
        log("VOICE", "CAUSE", "cause", true);          // 3회 중 2회 = 67%
        log("CHOICE", "MIND", "sad", true);            // 고르기는 안 섞인다

        ReportDTO r = reportService.of(1L);

        assertThat(r.express().total()).isEqualTo(3);
        assertThat(r.express().score()).isEqualTo(67);
    }

    @Test
    @DisplayName("지난주와 견주어 증감을 낸다")
    void deltaComparesWithLastWeek() {

        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);            // 이번 주 2/2 = 100%

        LocalDateTime old = LocalDateTime.now().minusDays(10);
        log("CHOICE", "MIND", "sad", true, old);
        log("CHOICE", "MIND", "sad", false, old);      // 지난주 1/2 = 50%

        assertThat(reportService.of(1L).understand().delta()).isEqualTo(50);
    }

    /**
     * SRS-2 는 점수가 높을수록 사회적 어려움이 크다. 체크리스트도 같은 방향이라
     * 뒤집어 지수로 쓴다 — 뒤집는 걸 빠뜨리면 가장 어려워하는 아이가 100점이 된다.
     */
    @Test
    @DisplayName("사회성은 체크리스트를 뒤집어 지수로 만든다 (높은 응답 = 낮은 지수)")
    void socialInvertsChecklist() {

        answer("사회적 상호작용", 7);                   // 가장 어려워함
        answer("감정 표현", 7);
        answer("감정 이해", 7);

        assertThat(reportService.of(1L).social().score()).isZero();

        checklist.clear();
        answer("사회적 상호작용", 1);                   // 가장 잘함
        answer("감정 표현", 1);
        answer("감정 이해", 1);

        assertThat(reportService.of(1L).social().score()).isEqualTo(100);
    }

    /**
     * 기록이 적을 때는 체크리스트가 말한다.
     *
     * <p>⚠ 예전에는 무조건 절반씩이었다. 그러면 미션 4회짜리 성공률이 지수의 절반을
     * 좌우한다 — 네 번 중 세 번은 우연으로도 나온다(2026-08-20 지적).
     * 무게는 n/(n+20) 이라 4회면 수행이 17% 만 차지한다.
     */
    @Test
    @DisplayName("미션이 몇 번 안 되면 체크리스트 쪽에 무게가 실린다")
    void socialLeansOnChecklistWhenFewMissions() {

        answer("사회적 상호작용", 4);                   // (7-4)/6 = 50점
        answer("감정 표현", 4);
        answer("감정 이해", 4);

        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", false);           // 미션 75%, 4회

        // 무게 4/(4+20) = 0.167 → 50 × 0.833 + 75 × 0.167 = 54.2 → 54
        assertThat(reportService.of(1L).social().score()).isEqualTo(54);
    }

    /**
     * 기록이 쌓이면 실제 수행 쪽으로 무게가 옮겨 간다 — 같은 성공률이라도 지수가 올라간다.
     * 이게 절반 고정과의 핵심 차이다.
     */
    @Test
    @DisplayName("미션이 쌓일수록 실제 수행이 지수를 끌고 간다")
    void socialShiftsToMissionsAsRecordsGrow() {

        answer("사회적 상호작용", 4);                   // 체크리스트 50점
        answer("감정 표현", 4);
        answer("감정 이해", 4);

        /* 성공률은 위 시험과 똑같이 75% 인데 횟수만 60회로 늘린다 */
        for (int i = 0; i < 60; i++) {
            log("CHOICE", "MIND", "sad", i % 4 != 3);
        }

        // 무게 60/(60+20) = 0.75 → 50 × 0.25 + 75 × 0.75 = 68.75 → 69
        assertThat(reportService.of(1L).social().score())
                .as("4회일 때(54)보다 확실히 높아야 한다 — 같은 성공률이지만 근거가 두텁다")
                .isEqualTo(69);
    }

    @Test
    @DisplayName("감정별 이해도는 잘하는 감정이 위로 온다")
    void emotionRatesSortedByRate() {

        log("CHOICE", "MIND", "happy", true);
        log("CHOICE", "MIND", "happy", true);          // 기쁨 100%
        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "sad", false);           // 슬픔 50%

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
        /* 근거 줄은 비운다 — 지수 0% 옆에 '아직 없어요' 를 또 적지 않는다(2026-08-18 요청) */
        assertThat(r.understand().basis()).isEmpty();
        /* 체크리스트도 없으면 사회성은 가운데(50) — 아무것도 안 했는데 최하로 보이면 안 된다 */
        assertThat(r.social().score()).isEqualTo(50);
    }

    /**
     * 온보딩만 마친 상태.
     *
     * <p>예전에는 사회성만 값이 있고 감정 이해·표현은 0% 였다. 체크리스트에 그 두 영역
     * 문항이 있는데도 답이 쓰이지 않았기 때문이다(2026-08-19 지적). 0% 는 '아직 안 했다'
     * 가 아니라 '다 틀렸다' 로 읽히므로, 학습 전에는 제 영역 체크리스트를 출발점으로 쓴다.
     */
    @Test
    @DisplayName("학습 전에는 감정 이해·표현도 제 영역 체크리스트를 출발점으로 쓴다")
    void beforeLearningEachMetricUsesItsOwnChecklist() {

        answer("감정 이해", 1);                       // 가장 잘함 → (7-1)/6 = 100
        answer("감정 표현", 7);                       // 가장 어려워함 → 0
        answer("사회적 상호작용", 4);

        ReportDTO r = reportService.of(1L);

        assertThat(r.understand().score()).isEqualTo(100);
        assertThat(r.express().score()).isZero();

        /* 응답이 없는 영역은 가운데(50) — 안 물어본 것을 최하로 두지 않는다 */
        checklist.clear();
        answer("사회적 상호작용", 4);

        ReportDTO only = reportService.of(1L);
        assertThat(only.understand().score()).isEqualTo(50);
        assertThat(only.express().score()).isEqualTo(50);
    }

    /**
     * 학습이 시작되면 이 지표는 아이가 실제로 해낸 것을 보여 주는 자리다.
     * 체크리스트를 섞으면 학습 결과가 흐려진다 — 기록이 있으면 성공률 그대로 쓴다.
     */
    @Test
    @DisplayName("기록이 생기면 체크리스트를 섞지 않고 미션 성공률을 그대로 쓴다")
    void afterLearningUsesMissionRateOnly() {

        answer("감정 이해", 7);                       // 체크리스트로는 0점인 아이

        log("CHOICE", "MIND", "sad", true);
        log("CHOICE", "MIND", "happy", true);          // 미션은 2회 중 2회 = 100%

        assertThat(reportService.of(1L).understand().score())
                .as("체크리스트가 섞이면 100 이 아니라 50 이 된다")
                .isEqualTo(100);
    }

    /**
     * 레이더 축은 위에서 시작해 시계방향으로 그려진다. 순서가 흔들리면 라벨과 값이 어긋나
     * '감정 표현이 낮다' 를 '사회적 상호작용이 낮다' 로 읽게 된다.
     */
    @Test
    @DisplayName("레이더 축은 순서가 고정이고, 응답이 없는 영역은 가운데(50)로 둔다")
    void radarAxesAreFixedAndOrdered() {

        answer("감정 이해", 1);              // 가장 잘함 → 100
        answer("사회적 상호작용", 7);        // 가장 어려워함 → 0
        /* 감정 표현은 응답 없음 → 50 */

        List<ReportDTO.DomainIndex> axes = reportService.of(1L).domains();

        assertThat(axes).extracting(ReportDTO.DomainIndex::label)
                .containsExactly("감정 이해", "감정 표현", "사회적 상호작용");

        assertThat(axes.get(0).score()).isEqualTo(100);
        assertThat(axes.get(1).score()).isEqualTo(50);
        assertThat(axes.get(2).score()).isZero();
    }

    /**
     * 감정 조절은 <b>일부러</b> 축에서 뺐다(2026-08-20 확정).
     * 사회성 지수 계산에 안 쓰는 값을 같은 화면에 그리면 "왜 반영이 안 되나" 를 묻게 된다.
     * 응답이 있어도 축으로 나오면 안 된다 — 빠뜨린 것이 아니라는 뜻이다.
     */
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
