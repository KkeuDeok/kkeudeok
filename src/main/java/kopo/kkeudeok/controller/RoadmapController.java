package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.service.IRoadmapService;
import kopo.kkeudeok.service.impl.LearningPreparer;
import kopo.kkeudeok.util.SessionKeys;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 학습 로드맵 API
@Slf4j
@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final IRoadmapService roadmapService;
    private final LearningPreparer learningPreparer;

    private final kopo.kkeudeok.mapper.IStorySessionMapper sessionMapper;

    @org.springframework.beans.factory.annotation.Value("${kkeudeok.story.daily-goal:3}")
    private int dailyGoal;

    @GetMapping
    public ResponseEntity<View> get(@RequestParam(required = false) Long childId,
                                   HttpSession session) {

        Long id = SessionKeys.childId(session, childId);

        if (id == null) {
            return ResponseEntity.noContent().build();
        }

        RoadmapDTO roadmap = roadmapService.get(id);

        return roadmap == null ? ResponseEntity.noContent().build()
                               : ResponseEntity.ok(view(roadmap));
    }

    @PostMapping("/regenerate")
    public ResponseEntity<View> regenerate(@RequestParam(required = false) Long childId,
                                          HttpSession session) {
        Long id = SessionKeys.childId(session, childId);

        if (id == null) {
            return ResponseEntity.noContent().build();
        }

        RoadmapDTO roadmap = roadmapService.regenerate(id);

        return roadmap == null ? ResponseEntity.noContent().build()
                               : ResponseEntity.ok(view(roadmap));
    }

    @GetMapping("/status")
    public ResponseEntity<Status> status(@RequestParam(required = false) Long childId,
                                         HttpSession session) {

        Long id = SessionKeys.childId(session, childId);
        IRoadmapService.Prep prep = learningPreparer.status(id);

        if (prep == IRoadmapService.Prep.FAILED) {
            learningPreparer.prepareAsync(id);
        }

        return ResponseEntity.ok(Status.builder()
                .ready(prep == IRoadmapService.Prep.READY)
                .state(prep.name())
                .build());
    }

    @Builder
    public record Status(boolean ready, String state) {
    }

    private View view(RoadmapDTO roadmap) {
        return View.builder()
                .roadmapId(roadmap.getRoadmapId())
                .roadmapType(roadmap.getRoadmapType())
                .currentWeek(roadmapService.currentWeek(roadmap))
                .doneWeeks(roadmapService.doneWeeks(roadmap.getChildId(), roadmap))
                .todayDone(sessionMapper.countTodayCompleted(roadmap.getChildId()))
                .dailyGoal(dailyGoal)
                .plan(roadmap.getPlan())
                .thisWeek(roadmapService.currentWeekPlan(roadmap))
                .build();
    }

    @Builder
    public record View(
            Long roadmapId,

            String roadmapType,

            int currentWeek,

            java.util.Set<Integer> doneWeeks,

            int todayDone,

            int dailyGoal,

            RoadmapPlanDTO plan,

            RoadmapPlanDTO.Week thisWeek
    ) {
    }
}
