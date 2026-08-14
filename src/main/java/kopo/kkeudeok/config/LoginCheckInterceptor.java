package kopo.kkeudeok.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 문지기 — 컨트롤러에 닿기 전에 요청을 가로채 세션을 확인한다.
 *
 * 왜 컨트롤러마다 안 넣고 여기서 하나
 *  - 마이페이지 화면이 6개라 컨트롤러에 넣으면 같은 코드가 6벌이 되고,
 *    화면을 새로 추가할 때 빠뜨리면 그 화면만 조용히 뚫린다.
 *  - 어디에 걸지는 {@link WebConfig} 가 정한다.
 *
 * ⚠ 화면(GET) 전용이다. POST API 는 JSON 을 기대하는데 여기서 로그인 화면(HTML)으로
 *   리다이렉트하면 fetch 쪽에서 파싱 오류가 난다 — API 는 각자 세션을 본다.
 */
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    /** 로그인 성공 시 AuthApiController 가 세션에 넣는 값. 이게 있으면 로그인된 것이다. */
    public static final String SS_USER_ID = "SS_USER_ID";

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

        // 브라우저가 이 화면을 저장하지 못하게 한다.
        // 없으면 로그아웃 뒤 '뒤로가기' 로 지난 화면이 그대로 되살아난다 — 서버에 다시 묻지 않기 때문에
        // 이 인터셉터가 실행조차 되지 않는다(2026-08-14 확인, 응답에 Cache-Control 이 아예 없었다).
        // no-store 는 뒤로가기 캐시(bfcache)까지 막아 뒤로가기해도 서버에 다시 요청하게 만든다.
        response.setHeader("Cache-Control", "no-store");

        // getSession(false) — 세션이 없으면 null 을 준다.
        // 그냥 getSession() 을 쓰면 없을 때 새로 만들어 버려서 "빈 세션"이 생긴다.
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SS_USER_ID) == null) {
            log.info("로그인 안 된 접근 차단 — {}", request.getRequestURI());
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
