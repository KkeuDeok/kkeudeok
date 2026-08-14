package kopo.kkeudeok.controller;

import java.security.SecureRandom;

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
public class AuthApiController {

    private final IUserService userService;
    private final IMailService mailService;

    private static final String SS_AUTH_CODE = "SS_AUTH_CODE";
    private static final String SS_AUTH_EMAIL = "SS_AUTH_EMAIL";
    private static final String SS_AUTH_EXPIRE = "SS_AUTH_EXPIRE";

    public static final String SS_FOUND_ID = "SS_FOUND_ID";

    public static final String SS_PIN_SET = "SS_PIN_SET";

    /** 보호자 확인(게이트)을 통과했다는 표. 마이페이지 하위 화면은 이게 있어야 열린다 */
    public static final String SS_PIN_OK = "SS_PIN_OK";

    /** PIN 재설정 본인 확인을 마쳤다는 1회용 표. 2단계 화면(UserController)도 이걸 본다 */
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

            // 최초 1회 전용이다. 이미 있는 PIN 을 여기서 덮을 수 있으면 게이트가 무의미해진다 —
            // 로그인만 돼 있으면 이 주소를 직접 불러 PIN 을 갈아 끼우고 들어갈 수 있다.
            // 바꾸는 길은 메일 인증을 거치는 /newParentPinProc 하나뿐이다.
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

    /* ================================================================
     * 보호자 PIN — 게이트 확인 · 재설정
     *
     * 전에는 정답 PIN(1234)이 gate.jsp 에 박혀 있었고 실패 횟수·잠금도 sessionStorage 였다.
     * 개발자도구로 값을 읽거나 지우면 그냥 열렸다 — 판정을 전부 서버로 옮긴다.
     *
     * ponytail: 실패 횟수·잠금을 HttpSession 에 둔다. 쿠키를 버리거나 시크릿 창을 새로 열면
     *   횟수가 0 부터 다시 시작한다. 계정 단위로 진짜 잠그려면 member 에 컬럼 두 개
     *   (pin_fail_count · pin_locked_until)를 추가해 DB 로 옮기면 된다 — 스키마 변경이라
     *   팀 합의가 필요해 지금은 세션에 둔다.
     * ================================================================ */

    /** 남은 잠금 시간(초). 0 이면 안 잠긴 상태. 게이트 화면 첫 그림도 이 값을 쓴다 */
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

    /** 마이페이지 게이트 — 입력한 PIN 이 맞는지 서버가 판정한다 */
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
                session.removeAttribute(SS_PIN_TRIES);   // 잠금이 풀리면 0회부터 다시 센다
                return lockedMsg(pinLockLeft(session));
            }

            session.setAttribute(SS_PIN_TRIES, tries);
            return msg(0, "PIN이 일치하지 않아요 (" + tries + "/" + PIN_MAX_TRY + "회)", "gatePin");

        } catch (Exception e) {
            log.error("verifyParentPinProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.", "gatePin");
        }
    }

    /**
     * PIN 재설정 1단계 — 메일 인증.
     *
     * 이메일은 파라미터로 받지 않고 세션의 계정에서 꺼낸다. 화면이 보내는 값을 믿으면
     * 자기 메일 주소를 실어 남의 계정 PIN 을 바꿀 수 있다(화면의 readonly 는 우회된다).
     *
     * ⚠ 이 단계가 없으면 게이트가 통째로 무의미해진다 — PIN 을 몰라도 [PIN을 잊었어요] 로 들어와
     *   아무 값이나 새로 정하면 그만이라 5회 잠금이 우회된다.
     */
    @PostMapping("/pinResetVerifyProc")
    public MsgDTO pinResetVerifyProc(@RequestParam String authCode, HttpSession session) {
        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.", "authCode");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);   // 이메일은 서비스가 복호화해 준다

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

    /** PIN 재설정 2단계 — 새 PIN 저장. 1단계 표를 쓰고 즉시 지운다 */
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

            session.removeAttribute(SS_PIN_RESET_OK);   // 표는 1회용이다
            clearPinLock(session);                      // 본인 확인을 마쳤으니 5회 잠금도 푼다
            session.setAttribute(SS_PIN_SET, true);
            session.setAttribute(SS_PIN_OK, true);      // 방금 확인했으므로 게이트를 다시 물을 필요가 없다

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
        // 인증번호를 받은 메일과 지금 조회하려는 메일이 달라지는 것을 막는다.
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

    /** 아이디 중복 확인 — 화면에서 아이디 칸을 벗어날 때 부른다. */
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

            // TODO signup-terms 에서 실제 체크값(특히 agree_marketing)을 넘겨받도록 바꿀 것.
            pDTO.setAgreeService(1);
            pDTO.setAgreePrivacy(1);
            pDTO.setAgreeSensitive(1);
            pDTO.setNotifyWeeklyReport(1);
            pDTO.setNotifyReminder(1);
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

            consumeAuthCode(session);   // 가입이 끝난 뒤에야 폐기한다
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

            // 이름이 틀렸을 뿐일 수 있다 — 인증번호는 살려 둬야 이름만 고쳐 다시 누른다.
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

    /** 1단계: 메일 인증이 끝나면 재설정 화면으로 갈 수 있는 표를 세션에 끊어 준다. */
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

    /** 2단계: 새 비밀번호 저장. 세션의 표를 쓰고 즉시 지운다. */
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

            // 비밀번호를 바꾸면 로그인 상태를 끊는다(2026-08-14 사용자 확정).
            // 비밀번호가 새 나가 바꾸는 경우가 대부분인데, 그 사람이 이미 로그인해 둔 세션이
            // 살아 있으면 바꾼 의미가 없다. 재설정 표(SS_PW_RESET_ID)도 같이 사라진다.
            //
            // ponytail: 세션이 서버 메모리에만 있어 '이 브라우저'만 확실히 끊긴다.
            //   다른 기기·다른 브라우저의 세션까지 끊으려면 로그인 세션을 계정별로 등록해 두는
            //   장치(세션 레지스트리)가 필요하다. 필요해지면 그때 붙일 것.
            session.invalidate();

            return msg(1, "비밀번호가 변경되었습니다.");

        } catch (Exception e) {
            log.error("newPasswordProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }
}
