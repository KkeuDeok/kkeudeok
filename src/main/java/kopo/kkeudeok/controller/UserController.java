package kopo.kkeudeok.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그인·회원가입 화면 라우팅 (프론트 구현분 확인용 골격).
 *
 * GET 매핑으로 JSP를 띄우는 것까지만 담당한다. 로그인 처리·회원가입 저장 등
 * POST 로직은 백엔드 담당이 이 클래스에 채우거나 별도 컨트롤러로 분리하면 된다.
 * 화면에서 [확인]·[다음] 버튼은 아직 auth-validate.js 가 클라이언트 검증 후
 * location.href 로 넘기는 데모 상태다.
 */
@Controller
public class UserController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup/terms")
    public String signupTerms() {
        return "auth/signup-terms";
    }

    @GetMapping("/signup/form")
    public String signupForm() {
        return "auth/signup-form";
    }

    @GetMapping("/signup/done")
    public String signupDone() {
        return "auth/signup-done";
    }

    @GetMapping("/find-id")
    public String findId() {
        return "auth/find-id";
    }

    @GetMapping("/find-id/result")
    public String findIdResult() {
        return "auth/find-id-result";
    }

    @GetMapping("/find-pw")
    public String findPwEmail() {
        return "auth/find-pw-email";
    }

    @GetMapping("/find-pw/new")
    public String findPwNew() {
        return "auth/find-pw-new";
    }

    @GetMapping("/find-pw/done")
    public String findPwDone() {
        return "auth/find-pw-done";
    }
}
