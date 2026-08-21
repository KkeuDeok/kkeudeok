package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryScenarioDTO;
import kopo.kkeudeok.dto.StoryStage;

// AI 이야기 생성
public interface IStoryAiService {

    record Brief(String place, java.util.List<String> recentTitles, java.util.List<String> laterTopics, boolean weekOpener) { }

    StoryScenarioDTO createScenario(ChildDTO child, String emotion, String dailyNote,
                                    Brief brief,
                                    RoadmapPlanDTO.Week week);

    StoryNodeDTO writeNode(ChildDTO child,
                           StoryScenarioDTO scenario,
                           StoryStage stage,
                           StoryRequestDTO.Next previous);
}
