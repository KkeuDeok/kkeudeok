package kopo.kkeudeok.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {

    /** 마이페이지 회원정보 조회용. 나머지 라우트는 화면만 띄우므로 서비스가 필요 없다. */
    private final IUserService userService;

    /** 아동 프로필·캐릭터 화면용 (dev) */
    @Autowired
    private IProfileService profileService;

    /**
     * 주소창에 localhost:8080 만 쳤을 때의 첫 화면.
     *
     * 세션을 보고 갈라준다 — 전에는 무조건 로그인 화면이라, 이미 로그인한 사람이
     * 새 창으로 들어오면 다시 로그인 화면을 봐야 했다(2026-08-14 지적).
     *
     * ⚠ PIN 검사를 같이 하는 이유 — dev 의 로그인·온보딩 흐름은 "PIN 이 없으면 먼저 만들게"
     *   보내는데(AuthApiController.nextStep), 여기서 곧바로 /dashboard 로 보내면
     *   주소창에 localhost:8080 만 쳐서 온보딩을 통째로 건너뛸 수 있다. 순서를 맞춘다.
     * ⚠ /login 은 그대로 둔다. 계정을 바꾸려고 일부러 오는 경우가 있어서다.
     */
    @GetMapping("/")
    public String root(HttpSession session) {
        if (CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")).isEmpty()) {
            return "redirect:/login";
        }
        if (!Boolean.TRUE.equals(session.getAttribute(AuthApiController.SS_PIN_SET))) {
            return "redirect:/onboarding/pin";
        }
        return "redirect:/dashboard";
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

    /**
     * 비밀번호 찾기(=변경) 1단계.
     *
     * 화면 하나를 두 상황이 같이 쓴다.
     *  · 비로그인 — 비밀번호를 잊은 사람. 이메일을 직접 입력한다.
     *  · 로그인   — 마이페이지에서 [비밀번호 변경] 으로 온 사람.
     *               자기 이메일을 다시 타이핑하는 게 어색하고 오타·오입력 위험도 있어
     *               세션의 계정 이메일을 채워 주고 수정은 막는다(화면에서 readonly).
     *
     * ⚠ readonly 는 편의·실수 방지용이다. 개발자도구로 다른 주소를 보낼 수는 있지만,
     *   그러면 인증번호가 그 주소로 갈 뿐이라 메일함을 못 여는 이상 진행되지 않는다
     *   — 이 흐름의 본인 확인은 어차피 '메일을 받을 수 있는가' 이다.
     */
    @GetMapping("/find-pw")
    public String findPwEmail(HttpSession session, ModelMap model) throws Exception {

        String loginId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (!loginId.isEmpty()) {
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);   // 이메일은 서비스가 복호화해 준다
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

    /* ---------- 마이페이지 (게이트 통과 후 탭 3개) ---------- */

    /**
     * 보호자 확인(게이트).
     *
     * 잠금이 걸려 있으면 남은 시간을 화면에 같이 넘긴다 — 새로고침해도 잠금이 보여야 한다.
     * 남은 시간을 화면이 스스로 계산하면 시계를 돌려 풀 수 있으므로 서버 값만 쓴다.
     */
    @GetMapping("/mypage")
    public String mypage(HttpSession session, ModelMap model) {
        model.addAttribute("pinLockLeft", AuthApiController.pinLockLeft(session));
        return "mypage/gate";
    }

    /**
     * 게이트를 통과했는지. 안 했으면 게이트로 돌려보낸다.
     *
     * 전에는 게이트 통과 여부가 화면 JS 안에만 있어서 /mypage/account 를 주소창에 직접 치면
     * PIN 을 한 번도 안 넣고 들어갈 수 있었다. 표는 세션에 있고 로그아웃하면 같이 사라진다.
     *
     * ponytail: 표에 만료가 없다 — 한 번 통과하면 그 세션 동안 유지된다.
     *   자리를 비운 사이 아이가 여는 걸 막으려면 통과 시각을 같이 저장해 n분 뒤 다시 묻게 하면 된다.
     */
    private boolean pinPassed(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(AuthApiController.SS_PIN_OK));
    }

    /** PIN 재설정 1단계. 인증번호를 받을 이메일은 계정에서 가져온다(화면에 박아 두지 않는다) */
    @GetMapping("/mypage/pin-reset")
    public String mypagePinReset(HttpSession session, ModelMap model) throws Exception {

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")));

        UserDTO rDTO = userService.getUserInfo(pDTO);   // 이메일은 서비스가 복호화해 준다

        if (rDTO == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("myEmail", CmmUtil.nvl(rDTO.getEmail()));
        return "mypage/pin-reset";
    }

    /** PIN 재설정 2단계 — 1단계 표가 없으면 열지 않는다(주소만 쳐서 새 PIN 을 정하는 것을 막는다) */
    @GetMapping("/mypage/pin-reset/new")
    public String mypagePinResetNew(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(AuthApiController.SS_PIN_RESET_OK))) {
            return "redirect:/mypage/pin-reset";
        }
        return "mypage/pin-reset-new";
    }

    /**
     * 회원정보 탭. 화면에 박아 두었던 예시 값(김민서·jiu@example.com…)을 실제 회원 정보로 바꾼다.
     *
     * 대상은 세션의 로그인 아이디다 — 주소나 파라미터로 받으면 남의 정보를 열어 볼 수 있다.
     * 여기까지 오려면 LoginCheckInterceptor 를 통과해야 하므로 세션은 반드시 있다.
     * 그래도 조회 결과가 null 이면(예: 다른 창에서 계정을 지운 뒤) 세션이 낡은 것이라 로그인으로 보낸다.
     */
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

    // 아이 정보 조회 로직 - SessionKeys를 사용하여 숫자형(Long) 아이디를 안전하게 가져옵니다.
    @GetMapping("/mypage/child")
    public String mypageChild(HttpSession session, ModelMap model) throws Exception {

        if (!pinPassed(session)) {
            return "redirect:/mypage";
        }

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

        if (!pinPassed(session)) {
            return "redirect:/mypage";
        }

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