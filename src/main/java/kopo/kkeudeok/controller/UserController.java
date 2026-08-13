package kopo.kkeudeok.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * 화면 라우팅 전담. GET 으로 JSP 를 띄우는 것까지만 한다.
 *
 * ⚠ 로그인·회원가입·아이디/비밀번호 찾기의 POST 처리는 여기 넣지 말 것 —
 *   전부 {@link AuthApiController} 에 모여 있다(2026-08-13 병합).
 */
@Controller
public class UserController {

    /** 주소창에 localhost:8080 만 쳤을 때 404 대신 로그인으로 보낸다. */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /* ---------- 회원가입 3단계 ---------- */

    /** 1단계 약관동의 */
    @GetMapping("/signup/terms")
    public String signupTerms() {
        return "auth/signup-terms";
    }

    /** 2단계 정보입력 */
    @GetMapping("/signup/form")
    public String signupForm() {
        return "auth/signup-form";
    }

    /** 3단계 가입완료 */
    @GetMapping("/signup/done")
    public String signupDone() {
        return "auth/signup-done";
    }

    /* ---------- 아이디 찾기 ---------- */

    @GetMapping("/find-id")
    public String findId() {
        return "auth/find-id";
    }

    /**
     * 찾은 아이디를 보여 준다. 값은 findIdProc 이 세션에 넣어 둔 것을 한 번 읽고 지운다
     * (주소창에 실으면 방문 기록·리퍼러에 남는다). 인증 없이 들어오면 아이디찾기로 되돌린다.
     */
    @GetMapping("/find-id/result")
    public String findIdResult(HttpSession session, ModelMap model) {
        String foundId = (String) session.getAttribute(AuthApiController.SS_FOUND_ID);

        if (foundId == null) {
            return "redirect:/find-id";
        }

        session.removeAttribute(AuthApiController.SS_FOUND_ID);
        model.addAttribute("foundId", foundId);
        return "auth/find-id-result";
    }

    /* ---------- 비밀번호 찾기 ---------- */

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

    /* ---------- 온보딩(보호자) ---------- */

    @GetMapping("/onboarding/pin")
    public String onboardingPin() {
        return "onboarding/pin";
    }

    /**
     * PIN 설정 곰 버전 시안 — 흐름에 연결돼 있지 않다(직접 주소로만 접근).
     * 채택되면 로그인 성공 시 이동 경로를 이쪽으로 바꾸면 된다.
     */
    @GetMapping("/onboarding/pin-bear")
    public String onboardingPinBear() {
        return "onboarding/pin-bear";
    }

    @GetMapping("/onboarding/start")
    public String onboardingStart() {
        return "onboarding/start";
    }

    @GetMapping("/onboarding/profile")
    public String onboardingProfile() {
        return "onboarding/child-profile";
    }

    @GetMapping("/onboarding/character")
    public String onboardingCharacter() {
        return "onboarding/character";
    }

    @GetMapping("/onboarding/checklist")
    public String onboardingChecklist() {
        return "onboarding/checklist";
    }

    @GetMapping("/onboarding/checklist-2")
    public String onboardingChecklist2() {
        return "onboarding/checklist2";
    }

    @GetMapping("/onboarding/face-guide")
    public String onboardingFaceGuide() {
        return "onboarding/face-guide";
    }

    @GetMapping("/onboarding/face-capture")
    public String onboardingFaceCapture() {
        return "onboarding/face-capture";
    }

    @GetMapping("/onboarding/done")
    public String onboardingDone() {
        return "onboarding/done";
    }

    /* ---------- 보호자 앱 ---------- */

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/learn")
    public String learn() {
        return "learn/index";
    }

    /* ---------- 성장 리포트 (탭 4개, 첫 탭 = 감정 이해) ---------- */

    @GetMapping("/report")
    public String report() {
        return "report/understand";
    }

    @GetMapping("/report/express")
    public String reportExpress() {
        return "report/express";
    }

    @GetMapping("/report/social")
    public String reportSocial() {
        return "report/social";
    }

    @GetMapping("/report/weekly")
    public String reportWeekly() {
        return "report/weekly";
    }

    /* ---------- 마이페이지 (게이트 통과 후 탭 3개) ---------- */

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage/gate";
    }

    @GetMapping("/mypage/pin-reset")
    public String mypagePinReset() {
        return "mypage/pin-reset";
    }

    @GetMapping("/mypage/pin-reset/new")
    public String mypagePinResetNew() {
        return "mypage/pin-reset-new";
    }

    @GetMapping("/mypage/account")
    public String mypageAccount() {
        return "mypage/account";
    }

    @GetMapping("/mypage/child")
    public String mypageChild() {
        return "mypage/child";
    }

    @GetMapping("/mypage/character")
    public String mypageCharacter() {
        return "mypage/character";
    }

    /* ---------- 아동 학습 흐름(스토리) — 스텝 이야기/마음/왜?/표정/행동/칭찬 ----------
       화면만 늘어나고 화면별 로직이 없어 한 라우트로 받는다. 화면을 추가할 때
       JSP 만 만들고 아래 목록에 이름을 넣으면 되므로 서버 재시작이 필요 없다.
       (컨트롤러를 고치면 재시작이 필요한데 8080 서버 주인이 다른 세션일 때가 많다.)
       ⚠ 화이트리스트 밖은 404 — 임의 경로로 JSP 를 훑는 걸 막는다.
       감정 벌은 전부 `?emo=sad|angry|happy` 로 갈린다(기본 sad). */
    private static final Set<String> STORY_STEPS = Set.of(
            "home",          // 아동홈 — 학습 단계가 아니라 흐름의 입구다(스텝바·하단바 없음)
            "scene",         // 학습1 상황 이야기
            "feel",          // 학습2 마음 읽기
            "feel-hint",     // 학습2b 힌트
            "why",           // 학습3 이유 찾기 (듣는 중은 별도 화면이 아니라 같은 화면의 상태다)
            "face",          // 학습4 표정 따라하기
            "situation",     // 상황 선택
            "act",           // 학습5 동작 따라하기
            "result"         // 학습6 세션 결과
    );

    @GetMapping("/story/{step}")
    public String story(@PathVariable String step) {
        if (!STORY_STEPS.contains(step)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "story/" + step;
    }
}
