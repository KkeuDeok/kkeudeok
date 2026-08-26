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

/**
 * 로그인 문지기 — 컨트롤러에 닿기 전에 요청을 가로채 세션을 확인한다.
 *
 * 왜 컨트롤러마다 안 넣고 여기서 하나
 *  - 마이페이지 화면이 6개라 컨트롤러에 넣으면 같은 코드가 6벌이 되고,
 *    화면을 새로 추가할 때 빠뜨리면 그 화면만 조용히 뚫린다.
 *  - 어디에 걸지는 {@link WebConfig} 가 정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final IUserService userService;

    /** 로그인 성공 시 UserProcController 가 세션에 넣는 값. 이게 있으면 로그인된 것이다. */
    public static final String SS_USER_ID = "SS_USER_ID";

    /** 회원이 아직 살아 있는지 한 번 확인했다는 표시. 세션마다 조회는 한 번이면 된다. */
    public static final String SS_MEMBER_OK = "SS_MEMBER_OK";

    /**
     * 컨트롤러 실행 '전에' 호출된다.
     *
     * @return true  → 통과. 원래 가려던 컨트롤러가 실행된다
     *         false → 차단. 컨트롤러는 실행조차 안 된다
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 브라우저가 이 화면을 저장하지 못하게 한다.
        // 없으면 로그아웃 뒤 '뒤로가기' 로 지난 화면이 그대로 되살아난다 — 서버에 다시 묻지 않기 때문에
        // 이 인터셉터가 실행조차 되지 않는다(2026-08-14 확인, 응답에 Cache-Control 이 아예 없었다).
        // no-store 는 뒤로가기 캐시(bfcache)까지 막아 뒤로가기해도 서버에 다시 요청하게 만든다.
        response.setHeader("Cache-Control", "no-store");

        // getSession(false) — 세션이 없으면 null 을 준다.
        // 그냥 getSession() 을 쓰면 없을 때 새로 만들어 버려서 "빈 세션"이 생긴다.
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

    /**
     * 세션은 DB 밖에 산다. 회원이 탈퇴하거나 데이터를 지워도 세션은 그대로라
     * "로그인은 됐는데 그 사람이 없는" 상태가 만들어진다 — 화면은 예시값으로 채워진다.
     *
     * 조회는 세션당 한 번이면 된다. 살아 있는 것을 확인한 뒤에는 표시만 보고 넘어간다.
     */
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
            // DB 가 잠깐 흔들렸다고 모두를 로그아웃시키지는 않는다.
            log.warn("회원 확인에 실패해 이번에는 통과시킵니다 — member={}: {}", memberId, e.getMessage());
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
