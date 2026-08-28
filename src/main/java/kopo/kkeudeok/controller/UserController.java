package kopo.kkeudeok.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.util.SessionKeys;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    private final IChildService childService;

    @GetMapping("/")
    public String root(HttpSession session) {
        if (CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")).isEmpty()) {
            return "redirect:/login";
        }
        if (!Boolean.TRUE.equals(session.getAttribute(UserProcController.SS_PIN_SET))) {
            return "redirect:/onboarding/pin";
        }
        return "redirect:/dashboard";
    }

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
    public String findIdResult(HttpSession session, ModelMap model) {
        String foundId = (String) session.getAttribute(UserProcController.SS_FOUND_ID);

        if (foundId == null) {
            return "redirect:/find-id";
        }

        session.removeAttribute(UserProcController.SS_FOUND_ID);
        model.addAttribute("foundId", foundId);
        return "auth/find-id-result";
    }

    @GetMapping("/find-pw")
    public String findPwEmail(HttpSession session, ModelMap model) throws Exception {

        String loginId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (!loginId.isEmpty()) {
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);
            if (rDTO != null) {
                model.addAttribute("myEmail", CmmUtil.nvl(rDTO.getEmail()));
            }
        }

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

    @GetMapping("/onboarding/pin")
    public String onboardingPin(HttpSession session) {
        if (session.getAttribute("SS_USER_ID") == null) {
            return "redirect:/login";
        }
        if (Boolean.TRUE.equals(session.getAttribute(UserProcController.SS_PIN_SET))) {
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

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/learn")
    public String learn() {
        return "learn/index";
    }

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

    @GetMapping("/mypage")
    public String mypage(HttpSession session, ModelMap model) {
        model.addAttribute("pinLockLeft", UserProcController.pinLockLeft(session));
        return "mypage/gate";
    }

    private boolean pinPassed(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(UserProcController.SS_PIN_OK));
    }

    @GetMapping("/mypage/pin-reset")
    public String mypagePinReset(HttpSession session, ModelMap model) throws Exception {

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")));

        UserDTO rDTO = userService.getUserInfo(pDTO);

        if (rDTO == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("myEmail", CmmUtil.nvl(rDTO.getEmail()));
        return "mypage/pin-reset";
    }

    @GetMapping("/mypage/pin-reset/new")
    public String mypagePinResetNew(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(UserProcController.SS_PIN_RESET_OK))) {
            return "redirect:/mypage/pin-reset";
        }
        return "mypage/pin-reset-new";
    }

    @GetMapping("/mypage/account")
    public String mypageAccount(HttpSession session, ModelMap model) throws Exception {

        if (!pinPassed(session)) {
            return "redirect:/mypage";
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")));

        UserDTO rDTO = userService.getUserInfo(pDTO);

        if (rDTO == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("user", rDTO);
        return "mypage/account";
    }

    @GetMapping("/mypage/child")
    public String mypageChild(HttpSession session, ModelMap model) throws Exception {

        if (!pinPassed(session)) {
            return "redirect:/mypage";
        }

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId != null) {

            ChildDTO child = childService.getChildByMember(memberId);

            model.addAttribute("child", child);
        }

        return "mypage/child";
    }

    @GetMapping("/mypage/character")
    public String mypageCharacter(HttpSession session, ModelMap model) throws Exception {

        if (!pinPassed(session)) {
            return "redirect:/mypage";
        }

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId != null) {

            ChildDTO child = childService.getChildByMember(memberId);

            model.addAttribute("child", child);
        }

        return "mypage/character";
    }

    private static final Set<String> STORY_STEPS = Set.of(
            "home",
            "scene",
            "feel",
            "feel-hint",
            "why",
            "face",
            "situation",
            "act",
            "result"
    );

    @GetMapping("/story/{step}")
    public String story(@PathVariable String step) {
        if (!STORY_STEPS.contains(step)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "story/" + step;
    }
}
