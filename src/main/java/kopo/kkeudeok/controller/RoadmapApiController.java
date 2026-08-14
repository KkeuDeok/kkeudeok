package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.service.IRoadmapService;
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
public class RoadmapApiController {

    private final IRoadmapService roadmapService;

    @GetMapping
    public ResponseEntity<View> get(@RequestParam(required = false) Long childId,
                                   HttpSession session) {
        return ResponseEntity.ok(view(roadmapService.getOrCreate(SessionKeys.childId(session, childId))));
    }

    @PostMapping("/regenerate")
    public ResponseEntity<View> regenerate(@RequestParam(required = false) Long childId,
                                          HttpSession session) {
        return ResponseEntity.ok(view(roadmapService.regenerate(SessionKeys.childId(session, childId))));
    }

    private View view(RoadmapDTO roadmap) {
        return View.builder()
                .roadmapId(roadmap.getRoadmapId())
                .roadmapType(roadmap.getRoadmapType())
                .currentWeek(roadmapService.currentWeek(roadmap))
                .plan(roadmap.getPlan())
                .thisWeek(roadmapService.currentWeekPlan(roadmap))
                .build();
    }

    @Builder
    public record View(
            Long roadmapId,

            String roadmapType,

            int currentWeek,

            RoadmapPlanDTO plan,

            RoadmapPlanDTO.Week thisWeek
    ) {
    }
}
