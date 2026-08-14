package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;

/**
 * 학습 로드맵 — 아이에게 맞춘 12주 커리큘럼
 */
public interface IRoadmapService {

    RoadmapDTO getOrCreate(Long childId);

    RoadmapDTO regenerate(Long childId);

    int currentWeek(RoadmapDTO roadmap);

    RoadmapPlanDTO.Week currentWeekPlan(RoadmapDTO roadmap);
}
