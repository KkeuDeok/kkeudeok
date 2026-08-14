package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.OnboardingRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.service.IOnboardingService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 온보딩 API
@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingApiController {

    private final IOnboardingService onboardingService;

    @PostMapping("/child")
    public ResponseEntity<Registered> register(@RequestBody OnboardingRequestDTO req,
                                               HttpSession session) {

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        ChildDTO child = onboardingService.register(memberId, req);

        session.setAttribute(SessionKeys.CHILD_ID, child.getChildId());

        return ResponseEntity.ok(Registered.builder()
                .childId(child.getChildId())
                .memberId(child.getMemberId())
                .callName(child.getCallName())
                .age(child.getAge())
                .characterType(child.getCharacterType())
                .build());
    }

    @GetMapping("/status")
    public ResponseEntity<Status> status(HttpSession session) {

        Long childId = SessionKeys.longOf(session, SessionKeys.CHILD_ID);

        return ResponseEntity.ok(Status.builder()
                .done(childId != null)
                .childId(childId)
                .build());
    }

    @Builder
    public record Registered(Long childId, Long memberId, String callName,
                             Integer age, String characterType) {
    }

    @Builder
    public record Status(boolean done, Long childId) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StoryResponseDTO.Fail> badRequest(IllegalArgumentException e) {

        log.warn("온보딩 잘못된 요청: {}", e.getMessage());

        return ResponseEntity.badRequest().body(StoryResponseDTO.Fail.builder()
                .code("BAD_REQUEST")
                .message(e.getMessage())
                .build());
    }
}
