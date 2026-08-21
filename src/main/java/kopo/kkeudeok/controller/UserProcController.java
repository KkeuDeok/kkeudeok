package kopo.kkeudeok.controller;

import java.security.SecureRandom;
import java.util.Set;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import kopo.kkeudeok.util.EncryptUtil;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProcController {

    private final IUserService userService;
    private final IMailService mailService;

    private static final String SS_USER_ID = "SS_USER_ID";

    private static final Set<String> RELATIONS = Set.of("어머니", "아버지", "조부모", "기타 보호자");

    private static final String SS_AUTH_CODE = "SS_AUTH_CODE";
    private static final String SS_AUTH_EMAIL = "SS_AUTH_EMAIL";
    private static final String SS_AUTH_EXPIRE = "SS_AUTH_EXPIRE";

    public static final String SS_FOUND_ID = "SS_FOUND_ID";

    public static final String SS_PIN_SET = "SS_PIN_SET";

    public static final String SS_PIN_OK = "SS_PIN_OK";

    public static final String SS_PIN_RESET_OK = "SS_PIN_RESET_OK";

    private static final String SS_PIN_TRIES = "SS_PIN_TRIES";
    private static final String SS_PIN_LOCK = "SS_PIN_LOCK";

    private static final int PIN_MAX_TRY = 5;
    private static final long PIN_LOCK_MS = 5 * 60 * 1000L;

    private static final String SS_PW_RESET_ID = "SS_PW_RESET_ID";

    private static final long CODE_VALID_MS = 3 * 60 * 1000L;

    private MsgDTO msg(int result, String text) {
        return msg(result, text, null);
    }

    private MsgDTO msg(int result, String text, String field) {
        MsgDTO dto = new MsgDTO();
        dto.setResult(result);
        dto.setMsg(text);
        dto.setField(field);
        return dto;
    }

    /* ================================================================
     * 로그인
     * ================================================================ */
    @PostMapping("/loginProc")
    public MsgDTO loginProc(@RequestParam String loginId,
                            @RequestParam String password,
                            HttpSession session) {

        log.info("{}.loginProc Start!", this.getClass().getName());

        try {
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            UserDTO rDTO = userService.getLogin(pDTO);

            if (rDTO == null) {
                return msg(0, "아이디 또는 비밀번호를 확인해주세요.", "password");
            }

            session.setAttribute("SS_USER_ID", rDTO.getLoginId());
            session.setAttribute("SS_USER_NAME", rDTO.getName());
            session.setAttribute(SessionKeys.MEMBER_ID, rDTO.getMemberId());
            session.setAttribute(SS_PIN_SET, !CmmUtil.nvl(rDTO.getParentPin()).isEmpty());

            Long childId = userService.childIdOf(rDTO.getMemberId());
            if (childId != null) {
                session.setAttribute(SessionKeys.CHILD_ID, childId);
            }

            MsgDTO res = msg(1, "환영합니다.");
            res.setNext(nextStep(rDTO));
            return res;

        } catch (Exception e) {
            log.error("loginProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    @PostMapping("/logoutProc")
    public MsgDTO logoutProc(HttpSession session) {
        session.invalidate();
        return msg(1, "로그아웃되었습니다.");
    }

    private String nextStep(UserDTO member) throws Exception {
        if (CmmUtil.nvl(member.getParentPin()).isEmpty()) {
            return "/onboarding/pin";
        }
        if (!userService.hasChild(member.getMemberId())) {
            return "/onboarding/start";
        }
        return "/dashboard";
    }

    @PostMapping("/parentPinProc")
    public MsgDTO parentPinProc(@RequestParam String pin, HttpSession session) {
        try {
            Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

            if (memberId == null) {
                return msg(0, "로그인이 필요합니다.", "pin");
            }
            if (!pin.matches("\\d{4}")) {
                return msg(0, "PIN 은 숫자 4자리여야 합니다.", "pin");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setMemberId(memberId);

            if (!userService.getParentPin(pDTO).isEmpty()) {
                MsgDTO res = msg(0, "이미 설정된 PIN 이 있어요. 바꾸려면 재설정을 이용해 주세요.", "pin");
                res.setNext("/mypage/pin-reset");
                return res;
            }

            pDTO.setParentPin(EncryptUtil.encHashSHA256(pin));

            if (userService.updateParentPin(pDTO) < 1) {
                return msg(0, "PIN 저장에 실패했습니다.", "pin");
            }

            session.setAttribute(SS_PIN_SET, true);

            MsgDTO res = msg(1, "보호자 PIN 이 설정되었습니다.");
            res.setNext(userService.hasChild(memberId) ? "/dashboard" : "/onboarding/start");
            return res;

        } catch (Exception e) {
            log.error("parentPinProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "pin");
        }
    }

    public static int pinLockLeft(HttpSession session) {
        Object until = session.getAttribute(SS_PIN_LOCK);

        if (!(until instanceof Long l)) {
            return 0;
        }

        long left = l - System.currentTimeMillis();
        return left > 0 ? (int) Math.ceil(left / 1000.0) : 0;
    }

    private MsgDTO lockedMsg(int left) {
        MsgDTO res = msg(0, PIN_MAX_TRY + "회 틀려서 " + (left / 60) + "분 "
                + String.format("%02d", left % 60)
                + "초 동안 잠겼어요. PIN을 잊었다면 아래에서 다시 설정하세요", "gatePin");
        res.setLockLeft(left);
        return res;
    }

    private void clearPinLock(HttpSession session) {
        session.removeAttribute(SS_PIN_TRIES);
        session.removeAttribute(SS_PIN_LOCK);
    }

    @PostMapping("/verifyParentPinProc")
    public MsgDTO verifyParentPinProc(@RequestParam String pin, HttpSession session) {
        try {
            Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

            if (memberId == null) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.", "gatePin");
            }

            int left = pinLockLeft(session);
            if (left > 0) {
                return lockedMsg(left);
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setMemberId(memberId);

            String saved = userService.getParentPin(pDTO);

            if (saved.isEmpty()) {
                MsgDTO res = msg(0, "보호자 PIN 을 아직 만들지 않았어요. 먼저 설정해 주세요.", "gatePin");
                res.setNext("/onboarding/pin");
                return res;
            }

            if (saved.equals(EncryptUtil.encHashSHA256(CmmUtil.nvl(pin)))) {
                clearPinLock(session);
                session.setAttribute(SS_PIN_OK, true);

                MsgDTO res = msg(1, "확인되었습니다.");
                res.setNext("/mypage/account");
                return res;
            }

            int tries = (session.getAttribute(SS_PIN_TRIES) instanceof Integer n ? n : 0) + 1;

            if (tries >= PIN_MAX_TRY) {
                session.setAttribute(SS_PIN_LOCK, System.currentTimeMillis() + PIN_LOCK_MS);
                session.removeAttribute(SS_PIN_TRIES);
                return lockedMsg(pinLockLeft(session));
            }

            session.setAttribute(SS_PIN_TRIES, tries);
            return msg(0, "PIN이 일치하지 않아요 (" + tries + "/" + PIN_MAX_TRY + "회)", "gatePin");

        } catch (Exception e) {
            log.error("verifyParentPinProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "gatePin");
        }
    }

    // PIN 재설정 1단계 — 메일 인증
    @PostMapping("/pinResetVerifyProc")
    public MsgDTO pinResetVerifyProc(@RequestParam String authCode, HttpSession session) {
        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.", "authCode");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);

            if (rDTO == null) {
                return msg(0, "회원 정보를 찾을 수 없습니다. 다시 로그인해 주세요.", "authCode");
            }

            String codeError = checkAuthCode(CmmUtil.nvl(rDTO.getEmail()), authCode, session);
            if (codeError != null) {
                return msg(0, codeError, "authCode");
            }

            session.setAttribute(SS_PIN_RESET_OK, true);
            consumeAuthCode(session);

            MsgDTO res = msg(1, "본인 확인이 끝났어요");
            res.setNext("/mypage/pin-reset/new");
            return res;

        } catch (Exception e) {
            log.error("pinResetVerifyProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "authCode");
        }
    }

    // PIN 재설정 2단계 — 새 PIN 저장. 1단계 표를 쓰고 즉시 지운다
    @PostMapping("/newParentPinProc")
    public MsgDTO newParentPinProc(@RequestParam String newPin, HttpSession session) {
        try {
            Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

            if (memberId == null) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.", "newPin");
            }
            if (!Boolean.TRUE.equals(session.getAttribute(SS_PIN_RESET_OK))) {
                return msg(0, "본인 확인이 필요합니다. 처음부터 다시 진행해 주세요.", "newPin");
            }
            if (!CmmUtil.nvl(newPin).matches("\\d{4}")) {
                return msg(0, "새 PIN 을 숫자 4자리로 입력해 주세요", "newPin");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setMemberId(memberId);
            pDTO.setParentPin(EncryptUtil.encHashSHA256(newPin));

            if (userService.updateParentPin(pDTO) < 1) {
                return msg(0, "PIN 변경에 실패했습니다. 잠시 후 다시 시도해 주세요.", "newPin");
            }

            session.removeAttribute(SS_PIN_RESET_OK);
            clearPinLock(session);
            session.setAttribute(SS_PIN_SET, true);
            session.setAttribute(SS_PIN_OK, true);

            MsgDTO res = msg(1, "보호자 PIN 이 변경되었습니다.");
            res.setNext("/mypage/account");
            return res;

        } catch (Exception e) {
            log.error("newParentPinProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "newPin");
        }
    }

    /* ================================================================
     * 인증번호 — 회원가입 · 아이디찾기 · 비밀번호찾기 공통
     * ================================================================ */
    @PostMapping("/sendAuthCodeProc")
    public MsgDTO sendAuthCodeProc(@RequestParam String email,
                                   @RequestParam(defaultValue = "signup") String kind,
                                   HttpSession session) {
        try {
            UserDTO pDTO = new UserDTO();
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            boolean exists = "Y".equals(CmmUtil.nvl(userService.getEmailExists(pDTO).getExistsYn()));

            if ("signup".equals(kind) && exists) {
                return msg(0, "이미 가입된 이메일입니다.", "email");
            }
            if (!"signup".equals(kind) && !exists) {
                return msg(0, "가입되지 않은 이메일입니다.", "email");
            }

            String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

            session.setAttribute(SS_AUTH_CODE, code);
            session.setAttribute(SS_AUTH_EMAIL, email);
            session.setAttribute(SS_AUTH_EXPIRE, System.currentTimeMillis() + CODE_VALID_MS);

            mailService.sendAuthCode(email, code);

            return msg(1, "인증번호를 보냈습니다. 메일함을 확인해 주세요.");

        } catch (Exception e) {
            log.error("sendAuthCodeProc 실패", e);
            return msg(2, "메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.", "email");
        }
    }

    private String checkAuthCode(String email, String authCode, HttpSession session) {
        Object saved = session.getAttribute(SS_AUTH_CODE);
        Object savedEmail = session.getAttribute(SS_AUTH_EMAIL);
        Object expire = session.getAttribute(SS_AUTH_EXPIRE);

        if (saved == null || expire == null) {
            return "인증번호를 먼저 전송해 주세요.";
        }
        if (System.currentTimeMillis() > (long) expire) {
            return "인증 시간이 지났습니다. 인증번호를 다시 전송해 주세요.";
        }
        if (!String.valueOf(savedEmail).equals(email)) {
            return "인증번호를 받은 이메일과 다릅니다.";
        }
        if (!String.valueOf(saved).equals(authCode)) {
            return "인증번호가 일치하지 않습니다.";
        }
        return null;
    }

    private void consumeAuthCode(HttpSession session) {
        session.removeAttribute(SS_AUTH_CODE);
        session.removeAttribute(SS_AUTH_EMAIL);
        session.removeAttribute(SS_AUTH_EXPIRE);
    }

    /* ================================================================
     * 회원가입
     * ================================================================ */
    @PostMapping("/checkLoginIdProc")
    public MsgDTO checkLoginIdProc(@RequestParam String loginId) {
        try {
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            boolean exists = "Y".equals(CmmUtil.nvl(userService.getLoginIdExists(pDTO).getExistsYn()));

            return exists ? msg(0, "이미 사용 중인 아이디입니다.", "loginId")
                          : msg(1, "사용할 수 있는 아이디입니다.", "loginId");
        } catch (Exception e) {
            log.error("checkLoginIdProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "loginId");
        }
    }

    @PostMapping("/signupProc")
    public MsgDTO signupProc(@RequestParam String userName,
                             @RequestParam String loginId,
                             @RequestParam String password,
                             @RequestParam String email,
                             @RequestParam String authCode,
                             HttpSession session) {

        log.info("{}.signupProc Start!", this.getClass().getName());

        try {
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setName(userName);
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            pDTO.setAgreeService(1);
            pDTO.setAgreePrivacy(1);
            pDTO.setAgreeSensitive(1);

            pDTO.setNotifyWeeklyReport(0);
            pDTO.setNotifyReminder(0);
            pDTO.setAgreeMarketing(0);

            if ("Y".equals(CmmUtil.nvl(userService.getLoginIdExists(pDTO).getExistsYn()))) {
                return msg(0, "이미 사용 중인 아이디입니다.", "loginId");
            }
            if ("Y".equals(CmmUtil.nvl(userService.getEmailExists(pDTO).getExistsYn()))) {
                return msg(0, "이미 가입된 이메일입니다.", "email");
            }

            String codeError = checkAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError, "authCode");
            }

            if (userService.insertUser(pDTO) != 1) {
                return msg(0, "회원가입에 실패했습니다.", "authCode");
            }

            consumeAuthCode(session);
            return msg(1, "회원가입이 완료되었습니다.");

        } catch (Exception e) {
            log.error("signupProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "authCode");
        }
    }

    /* ================================================================
     * 아이디 찾기
     * ================================================================ */
    @PostMapping("/findIdProc")
    public MsgDTO findIdProc(@RequestParam String userName,
                             @RequestParam String email,
                             @RequestParam String authCode,
                             HttpSession session) {
        try {
            String codeError = checkAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError, "authCode");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setName(userName);
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            UserDTO rDTO = userService.getFindId(pDTO);

            if (rDTO == null) {
                return msg(0, "일치하는 회원 정보가 없습니다.", "userName");
            }

            session.setAttribute(SS_FOUND_ID, CmmUtil.nvl(rDTO.getLoginId()));

            consumeAuthCode(session);
            return msg(1, "아이디를 찾았습니다.");

        } catch (Exception e) {
            log.error("findIdProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "authCode");
        }
    }

    /* ================================================================
     * 비밀번호 찾기 - 1) 메일 인증  2) 새 비밀번호 저장
     * ================================================================ */
    @PostMapping("/findPwProc")
    public MsgDTO findPwProc(@RequestParam String email,
                             @RequestParam String authCode,
                             HttpSession session) {
        try {
            String codeError = checkAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError, "authCode");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            UserDTO rDTO = userService.getFindPwUser(pDTO);

            if (rDTO == null) {
                return msg(0, "일치하는 회원 정보가 없습니다.", "email");
            }

            session.setAttribute(SS_PW_RESET_ID, rDTO.getLoginId());

            consumeAuthCode(session);
            return msg(1, "본인 확인이 완료되었습니다.");

        } catch (Exception e) {
            log.error("findPwProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "authCode");
        }
    }

    // 2단계: 새 비밀번호 저장
    @PostMapping("/newPasswordProc")
    public MsgDTO newPasswordProc(@RequestParam String newPassword, HttpSession session) {
        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_PW_RESET_ID));

            if (loginId.isEmpty()) {
                return msg(0, "비정상적인 접근입니다. 처음부터 다시 진행해 주세요.", "newPassword");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setPassword(EncryptUtil.encHashSHA256(newPassword));

            if (userService.newPasswordProc(pDTO) < 1) {
                return msg(0, "비밀번호 변경에 실패했습니다.", "newPassword");
            }

            session.invalidate();

            return msg(1, "비밀번호가 변경되었습니다.");

        } catch (Exception e) {
            log.error("newPasswordProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    @PostMapping("/updateUserInfoProc")
    public MsgDTO updateUserInfoProc(@RequestParam String userName,
                                     @RequestParam(required = false) String phone,
                                     @RequestParam(required = false) String relation,
                                     HttpSession session) {

        log.info("{}.updateUserInfoProc Start!", this.getClass().getName());

        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_USER_ID));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.");
            }

            String name = CmmUtil.nvl(userName).trim();
            String ph = CmmUtil.nvl(phone).trim();
            String rel = CmmUtil.nvl(relation).trim();

            if (name.isEmpty()) {
                return msg(0, "보호자 이름을 입력해 주세요.");
            }
            if (name.length() > 50) {
                return msg(0, "보호자 이름은 50자까지 입력할 수 있어요.");
            }
            if (!ph.isEmpty() && !ph.matches("[0-9-]{9,20}")) {
                return msg(0, "휴대폰 번호는 숫자와 - 만 써서 입력해 주세요.");
            }
            if (!rel.isEmpty() && !RELATIONS.contains(rel)) {
                return msg(0, "아이와의 관계를 다시 골라 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setName(name);
            pDTO.setPhone(ph.isEmpty() ? null : ph);
            pDTO.setRelation(rel.isEmpty() ? null : rel);

            if (userService.updateUserInfo(pDTO) < 1) {
                return msg(0, "저장에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            session.setAttribute("SS_USER_NAME", name);

            return msg(1, "회원정보를 저장했어요");

        } catch (Exception e) {
            log.error("updateUserInfoProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    // 계정 삭제
    @PostMapping("/deleteAccountProc")
    public MsgDTO deleteAccountProc(HttpSession session) {

        log.info("{}.deleteAccountProc Start!", this.getClass().getName());

        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_USER_ID));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);

            if (rDTO == null) {
                return msg(0, "이미 삭제된 계정입니다.");
            }

            UserDTO dDTO = new UserDTO();
            dDTO.setMemberId(rDTO.getMemberId());

            if (userService.deleteUser(dDTO) < 1) {
                return msg(0, "계정 삭제에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            session.invalidate();

            return msg(1, "계정이 삭제되었습니다.");

        } catch (Exception e) {
            log.error("deleteAccountProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }
}
