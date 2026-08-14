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

            반드시 지킬 것:
            - 한국어. 주제와 목표는 한 줄로 짧게.
            - 쉬운 것부터 어려운 것으로 쌓아 올린다. 감정을 알아보는 것이 먼저고,
              그 다음이 표현, 그 다음이 관계다.
            - 아이를 낮잡는 말, 치료·교정 같은 말은 쓰지 않는다. 연습·해 보기로 쓴다.
            - 답은 JSON 만. 설명이나 인사말을 붙이지 않는다.
            """;

    @Override
    public RoadmapPlanDTO createPlan(ChildDTO child, Map<String, Double> domainScores) {

        RoadmapPlanDTO fallback = FallbackRoadmap.plan();

        if (!gemini.isEnabled()) {
            return fallback;
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

                [JSON 형식]
                {
                  "stages": [{"name":"단계 이름","weeks":3}],
                  "weeks": [{"no":1,"stage":"단계 이름","topic":"주제 한 줄",
                             "situationType":"목록 중 하나","goal":"보호자에게 보여 줄 목표 한 문장"}]
                }
                """.formatted(
                TOTAL_WEEKS,
                child.getAge() == null ? "6세 정도" : child.getAge() + "세",
                nvl(child.getDisorderType(), "발달"),
                nvl(child.getSeverity(), "정도 미상"),
                scoreBrief(domainScores),
                TOTAL_WEEKS, TOTAL_WEEKS, TOTAL_WEEKS,
                SituationType.promptList()
        );

        // 12주 분량이어서 응답시간 늘림
        Optional<String> json = gemini.generateJson(SYSTEM, prompt, props.getRoadmapTimeoutSeconds());

        if (json.isEmpty()) {
            return fallback;
        }

        try {
            RoadmapPlanDTO plan = objectMapper.readValue(json.get(), RoadmapPlanDTO.class);

            if (!isUsable(plan)) {
                log.warn("AI 로드맵이 규칙에 안 맞아 정석 커리큘럼으로 진행합니다");
                return fallback;
            }

            normalize(plan);
            plan.setSource(RoadmapDTO.TYPE_AI);

            log.info("AI 로드맵 생성 — 단계 {}개, {}주", plan.getStages().size(), plan.getWeeks().size());
            return plan;

        } catch (Exception e) {
            log.warn("로드맵 JSON 파싱 실패 — 정석 커리큘럼으로 진행합니다: {}", e.getMessage());
            return fallback;
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
            // 상황 분류는 고정 목록이어야 이야기 생성으로 이어진다
            w.setSituationType(SituationType.normalize(w.getSituationType(), null).label());

            if (w.getGoal() == null || w.getGoal().isBlank()) {
                w.setGoal(w.getTopic() + "을(를) 연습해요");
            }
        }

        plan.setWeeks(weeks);
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
