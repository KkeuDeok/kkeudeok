package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.mapper.OnboardingMapper;
import kopo.kkeudeok.mapper.RoadmapMapper;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.service.IRoadmapAiService;
import kopo.kkeudeok.service.IRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 학습 로드맵
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService implements IRoadmapService {

    private final RoadmapMapper roadmapMapper;
    private final OnboardingMapper onboardingMapper;
    private final IChildService childService;
    private final IRoadmapAiService roadmapAiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RoadmapDTO getOrCreate(Long childId) {

        ChildDTO child = childService.getChild(childId);
        RoadmapDTO active = roadmapMapper.selectActive(child.getChildId());

        if (active != null) {
            unpack(active);
            return active;
        }

        log.info("아이 {} 의 로드맵이 없어 새로 만듭니다", child.getChildId());
        return create(child);
    }

    @Override
    @Transactional
    public RoadmapDTO regenerate(Long childId) {

        ChildDTO child = childService.getChild(childId);
        roadmapMapper.deactivateAll(child.getChildId());

        return create(child);
    }

    private RoadmapDTO create(ChildDTO child) {

        RoadmapPlanDTO plan = roadmapAiService.createPlan(child, domainScores(child.getChildId()));
        String type = RoadmapDTO.TYPE_AI.equals(plan.getSource())
                ? RoadmapDTO.TYPE_AI
                : RoadmapDTO.TYPE_STANDARD;

        RoadmapDTO roadmap = RoadmapDTO.builder()
                .childId(child.getChildId())
                .roadmapType(type)
                .stepData(toJson(plan))
                .isActive(true)
                .plan(plan)
                .build();

        roadmapMapper.insertRoadmap(roadmap);

        log.info("로드맵 생성 — roadmapId={}, child={}, type={}",
                roadmap.getRoadmapId(), child.getChildId(), type);

        return roadmap;
    }

    // 현재 주차
    @Override
    public int currentWeek(RoadmapDTO roadmap) {

        if (roadmap == null || roadmap.getPlan() == null) {
            return 1;
        }

        LocalDateTime from = roadmap.getCreatedAt();
        int total = Math.max(1, roadmap.getPlan().getTotalWeeks());

        if (from == null) {
            return 1;
        }

        long days = Duration.between(from, LocalDateTime.now()).toDays();
        int week = (int) (days / 7) + 1;

        return Math.max(1, Math.min(week, total));
    }

    @Override
    public RoadmapPlanDTO.Week currentWeekPlan(RoadmapDTO roadmap) {

        if (roadmap == null || roadmap.getPlan() == null
                || roadmap.getPlan().getWeeks() == null || roadmap.getPlan().getWeeks().isEmpty()) {
            return null;
        }

        int idx = currentWeek(roadmap) - 1;
        var weeks = roadmap.getPlan().getWeeks();

        return weeks.get(Math.max(0, Math.min(idx, weeks.size() - 1)));
    }

    /**
     * 체크리스트 영역별 평균 (영역 → 1~7).
     *
     * <p>⚠ 점수가 <b>높을수록 더 어려워한다</b>. 화면이 왼쪽 '그렇다'(1) ~ 오른쪽 '그렇지 않다'(7)
     * 로 되어 있기 때문이다. 뒤집어 읽으면 잘하는 영역에 주를 몰아주는 정반대 계획이 나온다.
     */
    private Map<String, Double> domainScores(Long childId) {

        List<ChecklistAnswerDTO> rows = onboardingMapper.selectChecklist(childId);

        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        Map<String, int[]> acc = new LinkedHashMap<>();

        for (ChecklistAnswerDTO r : rows) {
            if (r.getDomain() == null || r.getScore() == null) {
                continue;
            }
            int[] a = acc.computeIfAbsent(r.getDomain(), k -> new int[2]);
            a[0] += r.getScore();
            a[1] += 1;
        }

        Map<String, Double> out = new LinkedHashMap<>();
        acc.forEach((domain, a) -> out.put(domain, a[1] == 0 ? 0 : (double) a[0] / a[1]));

        return out;
    }

    private void unpack(RoadmapDTO roadmap) {

        try {
            RoadmapPlanDTO plan = objectMapper.readValue(roadmap.getStepData(), RoadmapPlanDTO.class);

            if (plan == null || plan.getWeeks() == null || plan.getWeeks().isEmpty()) {
                throw new IllegalStateException("주차가 비어 있습니다");
            }

            roadmap.setPlan(plan);
        } catch (Exception e) {
            log.warn("로드맵 {} 의 step_data 를 읽지 못해 정석 커리큘럼으로 보여 줍니다: {}",
                    roadmap.getRoadmapId(), e.getMessage());
            roadmap.setPlan(FallbackRoadmap.plan());
        }
    }

    private String toJson(RoadmapPlanDTO plan) {
        try {
            return objectMapper.writeValueAsString(plan);
        } catch (Exception e) {
            log.warn("로드맵을 JSON 으로 바꾸지 못했습니다: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(FallbackRoadmap.plan());
            } catch (Exception fatal) {
                return "{\"version\":1,\"totalWeeks\":12,\"stages\":[],\"weeks\":[]}";
            }
        }
    }
}
