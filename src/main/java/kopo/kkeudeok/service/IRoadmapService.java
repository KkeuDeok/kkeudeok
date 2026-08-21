package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;

// 학습 로드맵 — 아이에게 맞춘 12주 커리큘럼
public interface IRoadmapService {

    RoadmapDTO get(Long childId);

    RoadmapDTO getOrCreate(Long childId);

    java.util.Set<Integer> doneWeeks(Long childId, RoadmapDTO roadmap);

    RoadmapDTO regenerate(Long childId);

    enum Prep { READY, GENERATING, FAILED, NO_CHILD }

    int currentWeek(RoadmapDTO roadmap);

    RoadmapPlanDTO.Week currentWeekPlan(RoadmapDTO roadmap);
}
