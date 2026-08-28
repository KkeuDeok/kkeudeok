package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.mapper.IOnboardingMapper;
import kopo.kkeudeok.mapper.IRoadmapMapper;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.service.IRoadmapAiService;
import kopo.kkeudeok.service.IRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService implements IRoadmapService {

    private final IRoadmapMapper roadmapMapper;
    private final IOnboardingMapper onboardingMapper;
    private final IChildService childService;
    private final IRoadmapAiService roadmapAiService;
    private final ObjectMapper objectMapper;

    private final kopo.kkeudeok.mapper.IStorySessionMapper sessionMapper;

    @Override
    @Transactional(readOnly = true)
    public RoadmapDTO get(Long childId) {

        if (childId == null) {
            return null;
        }

        RoadmapDTO active = roadmapMapper.selectActive(childId);

        if (active != null) {
            unpack(active);
        }

        return active;
    }

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

    private final Set<Long> creating = ConcurrentHashMap.newKeySet();

    private RoadmapDTO create(ChildDTO child) {

        Long childId = child.getChildId();

        if (!creating.add(childId)) {
            log.info("아이 {} 의 로드맵을 이미 만들고 있어 겹쳐 만들지 않습니다", childId);
            return roadmapMapper.selectActive(childId);
        }

        try {
            return createLocked(child);
        } finally {
            creating.remove(childId);
        }
    }

    private RoadmapDTO createLocked(ChildDTO child) {
        RoadmapDTO exist = roadmapMapper.selectActive(child.getChildId());

        if (exist != null) {
            unpack(exist);
            return exist;
        }

        RoadmapPlanDTO plan = roadmapAiService.createPlan(child, domainScores(child.getChildId()));

        if (plan == null) {
            log.warn("아이 {} 의 로드맵을 만들지 못했습니다", child.getChildId());
            return null;
        }

        RoadmapDTO roadmap = RoadmapDTO.builder()
                .childId(child.getChildId())
                .roadmapType(RoadmapDTO.TYPE_AI)
                .stepData(toJson(plan))
                .isActive(true)
                .plan(plan)
                .build();

        roadmapMapper.deactivateAll(child.getChildId());
        roadmapMapper.insertRoadmap(roadmap);

        log.info("로드맵 생성 — roadmapId={}, child={}", roadmap.getRoadmapId(), child.getChildId());

        return roadmap;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> doneWeeks(Long childId, RoadmapDTO roadmap) {

        if (childId == null || roadmap == null || roadmap.getCreatedAt() == null) {
            return Set.of();
        }

        int total = roadmap.getPlan() == null ? 12 : Math.max(1, roadmap.getPlan().getTotalWeeks());

        Set<Integer> done = new HashSet<>();

        for (LocalDateTime at : sessionMapper.selectCompletedStartedAt(childId)) {

            if (at == null || at.isBefore(roadmap.getCreatedAt())) {
                continue;
            }

            int week = (int) (Duration.between(roadmap.getCreatedAt(), at).toDays() / 7) + 1;

            if (week >= 1 && week <= total) {
                done.add(week);
            }
        }

        return done;
    }

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
            log.warn("로드맵 {} 의 step_data 를 읽지 못했습니다: {}",
                    roadmap.getRoadmapId(), e.getMessage());
            roadmap.setPlan(null);
        }
    }

    private String toJson(RoadmapPlanDTO plan) {
        try {
            return objectMapper.writeValueAsString(plan);
        } catch (Exception e) {
            throw new IllegalStateException("로드맵을 JSON 으로 바꾸지 못했습니다", e);
        }
    }
}
