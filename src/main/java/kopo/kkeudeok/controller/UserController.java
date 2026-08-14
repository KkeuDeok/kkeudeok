package kopo.kkeudeok.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.util.CmmUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import kopo.kkeudeok.dto.ProfileDTO;
import kopo.kkeudeok.service.IProfileService;
import kopo.kkeudeok.util.SessionKeys; // SessionKeys import 추가 완료!

@Controller
public class UserController {

    @Autowired
    private IProfileService profileService;

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
    public String onboardingPin(HttpSession session) {
        if (session.getAttribute("SS_USER_ID") == null) {
            return "redirect:/login";
        }
        if (Boolean.TRUE.equals(session.getAttribute(AuthApiController.SS_PIN_SET))) {
            return "redirect:/onboarding/start";
        }
        return "onboarding/pin";
    }

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

    // 아이 정보 조회 로직 - SessionKeys를 사용하여 숫자형(Long) 아이디를 안전하게 가져옵니다.
    @GetMapping("/mypage/child")
    public String mypageChild(HttpSession session, ModelMap model) throws Exception {

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId != null) {
            ProfileDTO pDTO = new ProfileDTO();
            pDTO.setMemberId(memberId);

            // DB에서 아이 정보 가져오기
            ProfileDTO child = profileService.getProfile(pDTO);

            // JSP 화면에서 쓸 수 있도록 'child'라는 이름표를 붙여서 넘겨주기
            model.addAttribute("child", child);
        }

        return "mypage/child";
    }

    @GetMapping("/mypage/character")
    public String mypageCharacter(HttpSession session, ModelMap model) throws Exception {

        // 바로 위의 /mypage/child 와 동일하게 세션에서 회원 ID(memberId)를 안전하게 꺼냅니다.
        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId != null) {
            ProfileDTO pDTO = new ProfileDTO();
            pDTO.setMemberId(memberId); // 👈 setCharacterNickname 대신 setMemberId 사용!

            // DB에서 아이 정보 가져오기
            ProfileDTO child = profileService.getProfile(pDTO);

            // JSP 화면으로 'child' 전달
            model.addAttribute("child", child);
        }

        return "mypage/character";
    }

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