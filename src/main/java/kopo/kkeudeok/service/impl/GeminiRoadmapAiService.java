package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.config.GeminiProperties;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.service.IRoadmapAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// AI 12주 로드맵
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRoadmapAiService implements IRoadmapAiService {

    private final GeminiClient gemini;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;

    private static final int TOTAL_WEEKS = 12;

    private static final String SYSTEM = """
            너는 자폐·발달장애 아동의 사회성 학습 커리큘럼을 짜는 특수교육 전문가다.
            보호자가 읽을 12주 계획을 만든다.

            [계획의 근거]
            보건복지부 국립정신건강센터 「발달장애 아동청소년의 문제행동치료 가이드라인」의
            다음 원칙을 따른다.

            1. 문제행동을 없애는 것이 아니라 그 행동이 하던 일(관심 끌기·회피·요구·감각)을
               대신할 **대체 행동**을 익히게 한다. 못 하게 막는 계획을 짜지 않는다.
            2. 일이 벌어진 뒤에 다루기보다 **미리 환경을 고른다** — 예측 가능한 순서,
               미리 알려 주기, 고를 수 있게 하기가 앞쪽 주차에 온다.
            3. **기능적 의사소통**이 이르다 — 원하는 것과 싫은 것을 말이나 몸짓으로
               전할 수 있게 되는 것이 감정 표현·관계 맺기보다 앞선다.
            4. 잘한 것을 알아봐 주는 **강화 중심**으로 쓴다. 벌·제지·교정은 쓰지 않는다.
            5. 한 번에 하나씩, 작게 쪼개어 **여러 주에 걸쳐 반복**한다.
            6. 마지막 주차들은 배운 것을 **다른 장소·다른 사람에게로 넓히고 유지**하는 데 쓴다.

            [쌓는 순서]
            환경·일과 예측 → 기능적 의사소통 → 감정 알아보기 → 감정 표현 →
            대체 행동 익히기 → 또래·가족과의 상호작용 → 일반화와 유지

            [교육과정 근거]
            대상은 유치원·어린이집 나이(만 3~5세)와 초등 저학년이다. 다음을 함께 따른다.
            - 누리과정 **사회관계** 영역: 나를 알고 존중하기 / 더불어 생활하기 /
              사회에 관심 가지기. 특히 '나와 다른 사람의 감정을 알고 상황에 맞게 표현하기'.
            - 특수교육 기본 교육과정의 **사회성·의사소통** 영역: 또래와 상호작용하기,
              차례 지키기, 도움 요청하기, 자기 감정 조절하기.
            - 개별화교육계획(IEP)의 방식: 한 주에 목표 하나, 관찰 가능한 행동으로 쓴다.

            [⚠ 이 로드맵이 쓰이는 곳 — 가장 중요하다]
            각 주차는 그대로 한 편의 이야기가 된다. 그 이야기에서 아이는
            "친구가 지금 어떤 마음일까?" 와 "왜 그런 마음이 들었을까?" 를 카드로 고른다.
            그러므로 모든 주제와 목표는 반드시 이런 것이어야 한다.
            - 사람이 둘 이상 나오고, 그중 누군가가 감정을 느끼는 장면일 것.
            - 그 감정을 기쁨·슬픔·화남·놀람 중 하나로 고를 수 있을 것.

            아래와 같은 것은 절대 쓰지 않는다. 감정을 고를 장면이 없기 때문이다.
            - 시각 카드 고르기, 그림 카드 배열하기, 일과표 보기
            - 일기 쓰기, 관찰하기, 기록하기, 색칠하기, 오려 붙이기
            - 낱말 읽기, 숫자 세기, 따라 쓰기 같은 학습지형 활동
            - 보호자만 하는 일(환경 정리하기, 규칙 정하기, 미리 알려 주기)

            같은 뜻이라도 장면으로 바꿔 쓴다.
            - (X) 시각 카드로 순서 알기   → (O) 미끄럼틀 앞에서 차례를 기다려 보기
            - (X) 감정 일기 써 보기       → (O) 속상해하는 친구에게 마음을 물어보기
            - (X) 규칙을 미리 알려 주기   → (O) 먼저 하고 싶어 하는 친구에게 양보해 보기

            반드시 지킬 것:
            - 한국어. 주제와 목표는 한 줄로 짧게.
            - 아이를 낮잡는 말, 치료·교정·문제행동 같은 말은 보호자 화면에 쓰지 않는다.
              연습·해 보기·알아보기로 쓴다.
            - 목표는 보호자가 그 주에 무엇을 함께 해 보면 되는지 알 수 있게 쓴다.
            - 답은 JSON 만. 설명이나 인사말을 붙이지 않는다.
            """;

    @Override
    public RoadmapPlanDTO createPlan(ChildDTO child, Map<String, Double> domainScores) {

        if (!gemini.isEnabled()) {
            log.warn("AI 키가 없어 로드맵을 짜지 못했습니다");
            return null;
        }

        String prompt = """
                아래 아이를 위한 %d주 사회성 학습 로드맵을 짜라.

                [아이]
                - 나이: %s
                - 장애 유형: %s
                - 장애 정도: %s

                [발달 체크리스트] 영역별 평균 (1~7, 높을수록 더 어려워한다)
                %s

                [규칙]
                - 단계(stages)는 4~6개. 각 단계의 weeks 를 모두 더하면 정확히 %d 여야 한다.
                - 주차(weeks)는 정확히 %d개. no 는 1부터 %d까지 빠짐없이.
                - 각 주차의 stage 는 stages 의 name 중 하나와 정확히 같아야 한다.
                - situationType 은 반드시 다음 중 하나를 그대로 쓴다: %s
                - 장애 정도가 무거우면 한 주제를 여러 주에 걸쳐 천천히 간다.
                - 자폐 유형이면 사회적 상호작용과 감정 이해에 주를 더 준다.
                - 체크리스트 점수가 높은(어려워하는) 영역에 주를 더 주고, 앞쪽에 배치한다.
                - 앞의 [계획의 근거] 순서를 지킨다. 특히 기능적 의사소통이 감정 표현보다 앞선다.
                - 마지막 2주 이상은 배운 것을 다른 장소·다른 사람에게로 넓히고 유지하는 데 쓴다.
                - goal 은 "~하지 않기" 가 아니라 "~해 보기" 로 쓴다(대체 행동 중심).
                - topic 과 goal 은 사람 사이에 일이 벌어지는 장면으로 쓴다. 카드·일기·관찰·
                  색칠 같은 활동은 쓰지 않는다(위 [⚠ 이 로드맵이 쓰이는 곳] 참고).
                - situationType 은 그 주의 장면과 실제로 맞아야 한다. 아무거나 고르지 않는다.
                - 12주 동안 situationType 이 한쪽으로 몰리지 않게 한다. 같은 값을 3주 넘게
                  잇달아 쓰지 않는다 — 그러면 아이가 늘 같은 상황만 겪는다.

                [JSON 형식]
                {
                  "stages": [{"name":"단계 이름","weeks":3}],
                  "weeks": [{"no":1,"stage":"단계 이름","topic":"주제 한 줄",
                             "situationType":"목록 중 하나","goal":"보호자에게 보여 줄 목표 한 문장"}]
                }
                """.formatted(
                TOTAL_WEEKS,
                child.promptAge(),
                nvl(child.getDisorderType(), "발달"),
                nvl(child.getSeverity(), "정도 미상"),
                scoreBrief(domainScores),
                TOTAL_WEEKS, TOTAL_WEEKS, TOTAL_WEEKS,
                SituationType.promptList()
        );

        Optional<String> json = gemini.generateJson(
                SYSTEM, prompt, props.getRoadmapTimeoutSeconds(), props.getRoadmapModel());

        if (json.isEmpty()) {
            log.warn("AI 가 로드맵을 돌려주지 않았습니다");
            return null;
        }

        try {
            RoadmapPlanDTO plan = objectMapper.readValue(json.get(), RoadmapPlanDTO.class);

            if (!isUsable(plan)) {
                log.warn("AI 로드맵이 규칙에 안 맞아 쓰지 않습니다");
                return null;
            }

            normalize(plan);
            plan.setSource(RoadmapDTO.TYPE_AI);

            log.info("AI 로드맵 생성 — 단계 {}개, {}주", plan.getStages().size(), plan.getWeeks().size());
            return plan;

        } catch (Exception e) {
            log.warn("로드맵 JSON 파싱 실패 — 로드맵을 짜지 못했습니다: {}", e.getMessage());
            return null;
        }
    }

    // AI가 작성한 로드맵 검토
    private boolean isUsable(RoadmapPlanDTO plan) {

        if (plan == null || plan.getStages() == null || plan.getWeeks() == null) {
            return false;
        }
        if (plan.getStages().isEmpty() || plan.getWeeks().size() != TOTAL_WEEKS) {
            return false;
        }

        int sum = plan.getStages().stream().mapToInt(RoadmapPlanDTO.Stage::getWeeks).sum();
        if (sum != TOTAL_WEEKS) {
            return false;
        }

        List<String> names = plan.getStages().stream().map(RoadmapPlanDTO.Stage::getName).toList();

        for (int i = 0; i < plan.getWeeks().size(); i++) {
            RoadmapPlanDTO.Week w = plan.getWeeks().get(i);

            if (w == null || w.getTopic() == null || w.getTopic().isBlank()) {
                return false;
            }
            if (w.getNo() != i + 1) {
                return false;                       // 순서가 빠지거나 뒤섞였다
            }
            if (w.getStage() == null || !names.contains(w.getStage())) {
                return false;                       // 없는 단계를 가리킨다
            }
        }

        return true;
    }

    private void normalize(RoadmapPlanDTO plan) {

        plan.setVersion(1);
        plan.setTotalWeeks(TOTAL_WEEKS);

        List<RoadmapPlanDTO.Week> weeks = new ArrayList<>(plan.getWeeks());

        for (RoadmapPlanDTO.Week w : weeks) {
            SituationType type = SituationType.normalize(w.getSituationType(), null);
            w.setSituationType(type.label());

            if (isActivityNotScene(w.getTopic())) {
                log.info("{}주차 주제 '{}' 는 감정을 고를 장면이 아니라 바꿉니다", w.getNo(), w.getTopic());
                w.setTopic(sceneTopic(type));
                w.setGoal(null);
            }

            if (w.getGoal() == null || w.getGoal().isBlank() || isActivityNotScene(w.getGoal())) {
                w.setGoal(w.getTopic() + "을(를) 함께 해 봐요");
            }
        }

        plan.setWeeks(weeks);
    }

    private static final List<String> ACTIVITY_WORDS = List.of(
            "카드", "일기", "관찰", "기록", "일과표", "색칠", "오려", "붙이기",
            "따라 쓰", "따라쓰", "읽기", "숫자", "낱말", "그림책", "학습지",
            "규칙 정하", "환경 정리", "미리 알려");

    static boolean isActivityNotScene(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String v = text.trim();

        for (String bad : ACTIVITY_WORDS) {
            if (v.contains(bad)) {
                return true;
            }
        }

        return false;
    }

    static String sceneTopic(SituationType type) {
        return switch (type) {
            case WAIT_TURN -> "미끄럼틀 앞에서 차례를 기다려 보기";
            case SHARE     -> "먼저 하고 싶어 하는 친구에게 양보해 보기";
            case COMFORT   -> "속상해하는 친구를 토닥여 주기";
            case APOLOGIZE -> "부딪힌 친구에게 미안하다고 말해 보기";
            case CELEBRATE -> "잘한 친구와 함께 기뻐해 주기";
            case ASK_HELP  -> "어려울 때 선생님에게 도와 달라고 말해 보기";
            case GREET     -> "처음 만난 친구에게 먼저 인사해 보기";
            case EXPRESS   -> "지금 내 마음을 친구에게 말해 보기";
        };
    }

    private String scoreBrief(Map<String, Double> scores) {

        if (scores == null || scores.isEmpty()) {
            return "  (응답 없음 — 프로필만 보고 짠다)";
        }

        StringBuilder sb = new StringBuilder();
        scores.forEach((domain, avg) ->
                sb.append("  - %s: %.1f%n".formatted(domain, avg)));

        return sb.toString().stripTrailing();
    }

    private static String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
