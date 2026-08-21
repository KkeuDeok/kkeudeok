package kopo.kkeudeok.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChildSessionInterceptor implements HandlerInterceptor {

    private final IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return true;
        }

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId == null || SessionKeys.longOf(session, SessionKeys.CHILD_ID) != null) {
            return true;
        }

        try {
            Long childId = userService.childIdOf(memberId);

            if (childId != null) {
                session.setAttribute(SessionKeys.CHILD_ID, childId);
                log.info("세션에 아이가 없어 다시 붙였습니다 — member={} child={}", memberId, childId);
            }

        } catch (Exception e) {
            log.warn("아이 번호를 세션에 붙이지 못했습니다 — member={}", memberId, e);
        }

        return true;
    }
}
