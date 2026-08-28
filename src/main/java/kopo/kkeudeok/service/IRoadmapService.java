package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;

public interface IRoadmapService {

    RoadmapDTO get(Long childId);

    RoadmapDTO getOrCreate(Long childId);

    java.util.Set<Integer> doneWeeks(Long childId, RoadmapDTO roadmap);

    RoadmapDTO regenerate(Long childId);

    enum Prep { READY, GENERATING, FAILED, NO_CHILD }

    int currentWeek(RoadmapDTO roadmap);

    RoadmapPlanDTO.Week currentWeekPlan(RoadmapDTO roadmap);
}
