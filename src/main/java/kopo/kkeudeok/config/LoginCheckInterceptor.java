package kopo.kkeudeok.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final IUserService userService;

    public static final String SS_USER_ID = "SS_USER_ID";

    public static final String SS_MEMBER_OK = "SS_MEMBER_OK";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SS_USER_ID) == null) {
            log.debug("로그인 안 된 접근 차단 — {} {}", request.getMethod(), request.getRequestURI());
            deny(request, response);
            return false;
        }

        if (!memberStillExists(session)) {
            log.info("세션이 가리키는 회원이 없어 세션을 버립니다 — {} {}",
                    request.getMethod(), request.getRequestURI());

            session.invalidate();
            deny(request, response);
            return false;
        }

        return true;
    }

    private boolean memberStillExists(HttpSession session) {

        if (Boolean.TRUE.equals(session.getAttribute(SS_MEMBER_OK))) {
            return true;
        }

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId == null) {
            return true;
        }

        try {
            if (!userService.memberExists(memberId)) {
                return false;
            }
        } catch (Exception e) {
            log.warn("회원 확인 실패 — member={}: {}", memberId, e.getMessage());
            return true;
        }

        session.setAttribute(SS_MEMBER_OK, true);
        return true;
    }

    private void deny(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (wantsJson(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"result\":0,\"msg\":\"로그인이 필요합니다.\",\"next\":\"/login\"}");
            return;
        }

        response.sendRedirect("/login");
    }

    private boolean wantsJson(HttpServletRequest request) {

        if (request.getRequestURI().startsWith("/api/")) {
            return true;
        }

        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }

        String accept = request.getHeader("Accept");

        return accept != null && !accept.contains("text/html") && accept.contains("json");
    }
}
