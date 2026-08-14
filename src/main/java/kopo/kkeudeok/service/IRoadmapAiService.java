package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;

import java.util.Map;

// AI 로드맵 생성
public interface IRoadmapAiService {

    RoadmapPlanDTO createPlan(ChildDTO child, Map<String, Double> domainScores);
}
