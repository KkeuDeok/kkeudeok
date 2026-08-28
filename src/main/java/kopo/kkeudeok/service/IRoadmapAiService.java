package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;

import java.util.Map;

public interface IRoadmapAiService {

    RoadmapPlanDTO createPlan(ChildDTO child, Map<String, Double> domainScores);
}
