package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.service.IStoryService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 학습 세션 API
@Slf4j
@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController {

    private final IStoryService storyService;

    @PostMapping("/sessions")
    public ResponseEntity<StoryResponseDTO.Start> start(@RequestBody(required = false) StoryRequestDTO.Start req,
                                                       HttpSession session) {

        StoryRequestDTO.Start body = (req == null) ? new StoryRequestDTO.Start() : req;

        body.setChildId(SessionKeys.childId(session, body.getChildId()));

        return ResponseEntity.ok(storyService.start(body));
    }

    @GetMapping("/sessions/resume")
    public ResponseEntity<StoryResponseDTO.Resume> resume(@RequestParam(required = false) Long childId,
                                                         HttpSession session) {

        return ResponseEntity.ok(storyService.resume(SessionKeys.childId(session, childId)));
    }

    @GetMapping("/summary")
    public ResponseEntity<StoryResponseDTO.Summary> summary(@RequestParam(required = false) Long childId,
                                                            HttpSession session) {

        return ResponseEntity.ok(storyService.summary(SessionKeys.childId(session, childId)));
    }

    @PostMapping("/sessions/{sessionId}/next")
    public ResponseEntity<StoryResponseDTO.Next> next(@PathVariable Long sessionId,
                                                      @RequestBody(required = false) StoryRequestDTO.Next req,
                                                      HttpSession session) {

        StoryRequestDTO.Next body = (req == null) ? new StoryRequestDTO.Next() : req;

        return ResponseEntity.ok(storyService.next(sessionId, loginChild(session), body));
    }

    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<StoryResponseDTO.Finish> finish(@PathVariable Long sessionId,
                                                          @RequestBody StoryRequestDTO.Finish req,
                                                          HttpSession session) {

        return ResponseEntity.ok(storyService.finish(sessionId, loginChild(session), req));
    }

    private Long loginChild(HttpSession session) {
        return SessionKeys.longOf(session, SessionKeys.CHILD_ID);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StoryResponseDTO.Fail> badRequest(IllegalArgumentException e) {

        log.warn("스토리 API 잘못된 요청: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(StoryResponseDTO.Fail.builder()
                        .code("BAD_REQUEST")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<StoryResponseDTO.Fail> conflict(IllegalStateException e) {

        log.warn("스토리 API 흐름 오류: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(StoryResponseDTO.Fail.builder()
                        .code("CONFLICT")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StoryResponseDTO.Fail> error(Exception e) {

        log.error("스토리 API 오류", e);

        return ResponseEntity.internalServerError()
                .body(StoryResponseDTO.Fail.builder()
                        .code("ERROR")
                        .message("학습을 불러오지 못했어요")
                        .build());
    }
}
