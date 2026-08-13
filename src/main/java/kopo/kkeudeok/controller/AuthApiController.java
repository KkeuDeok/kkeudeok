package kopo.kkeudeok.controller;

import java.security.SecureRandom;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import kopo.kkeudeok.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 · 회원가입 · 아이디찾기 · 비밀번호찾기의 POST 처리를 모두 담당한다.
 * 응답은 전부 {@link MsgDTO} JSON 이고, 화면 이동은 auth-validate.js 가 결정한다.
 *
 * 저장·조회 규칙 (⚠ 어기면 조회가 안 맞는다)
 *  - password : EncryptUtil.encHashSHA256 로 해시해서 넣고, 같은 해시로 찾는다.
 *  - email    : EncryptUtil.encAES128CBC 로 암호화해서 넣고, 같은 암호문으로 찾는다.
 *               메일을 보낼 때만 decAES128CBC 로 되돌린다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthApiController {

    private final IUserService userService;
    private final IMailService mailService;

    /* 인증번호 관련 세션 키 — SS_ 는 Session 을 뜻하는 강의 규칙. */
    private static final String SS_AUTH_CODE = "SS_AUTH_CODE";
    private static final String SS_AUTH_EMAIL = "SS_AUTH_EMAIL";
    private static final String SS_AUTH_EXPIRE = "SS_AUTH_EXPIRE";

    /** 아이디찾기 결과 — 결과 화면에서 한 번 읽고 지운다. */
    public static final String SS_FOUND_ID = "SS_FOUND_ID";

    /** 비밀번호 재설정 허가 — 인증을 통과한 회원의 login_id 가 들어 있다. */
    private static final String SS_PW_RESET_ID = "SS_PW_RESET_ID";

    /** 인증번호 유효시간 3분 — 화면 타이머(180초)와 같은 값이어야 한다. */
    private static final long CODE_VALID_MS = 3 * 60 * 1000L;

    /** MsgDTO 를 만드는 짧은 도우미. new + set 2줄이 계속 반복돼서 뺐다. */
    private MsgDTO msg(int result, String text) {
        MsgDTO dto = new MsgDTO();
        dto.setResult(result);
        dto.setMsg(text);
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
                return msg(0, "아이디 또는 비밀번호를 확인해주세요.");
            }

            session.setAttribute("SS_USER_ID", rDTO.getLoginId());
            session.setAttribute("SS_USER_NAME", rDTO.getName());

            return msg(1, "환영합니다.");

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

    /* ================================================================
     * 인증번호 — 회원가입 · 아이디찾기 · 비밀번호찾기 공통
     * ================================================================ */

    /**
     * 인증번호를 만들어 메일로 보내고, 정답은 세션에 적어 둔다.
     *
     * @param kind signup | findId | findPw — 가입 여부 검사 방향이 반대라 나눠 본다.
     *             회원가입은 "이미 있으면" 막고, 찾기는 "없으면" 막는다.
     */
    @PostMapping("/sendAuthCodeProc")
    public MsgDTO sendAuthCodeProc(@RequestParam String email,
                                   @RequestParam(defaultValue = "signup") String kind,
                                   HttpSession session) {
        try {
            UserDTO pDTO = new UserDTO();
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            boolean exists = "Y".equals(CmmUtil.nvl(userService.getEmailExists(pDTO).getExistsYn()));

            if ("signup".equals(kind) && exists) {
                return msg(0, "이미 가입된 이메일입니다.");
            }
            if (!"signup".equals(kind) && !exists) {
                return msg(0, "가입되지 않은 이메일입니다.");
            }

            // 000000 ~ 999999 중 하나. 앞자리가 0이어도 6자리가 되도록 %06d 로 채운다.
            String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

            if (mailService.sendAuthCode(email, code) != 1) {
                return msg(2, "메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            // 정답은 서버(세션)에만 둔다. 화면에 내려보내면 검사할 이유가 없어진다.
            session.setAttribute(SS_AUTH_CODE, code);
            session.setAttribute(SS_AUTH_EMAIL, email);
            session.setAttribute(SS_AUTH_EXPIRE, System.currentTimeMillis() + CODE_VALID_MS);

            return msg(1, "인증번호를 보냈습니다. 메일함을 확인해 주세요.");

        } catch (Exception e) {
            log.error("sendAuthCodeProc 실패", e);
            return msg(2, "메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    /**
     * 세션에 적어 둔 인증번호와 대조. 통과하면 한 번 쓰고 지운다.
     * 화면(JS)에서도 검사하지만 그건 사용자 편의일 뿐 — 개발자도구로 우회되므로 서버가 다시 본다.
     *
     * @return null 이면 통과, 아니면 화면에 보여 줄 오류 메시지
     */
    private String verifyAuthCode(String email, String authCode, HttpSession session) {
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

        session.removeAttribute(SS_AUTH_CODE);      // 한 번 쓰면 폐기 — 재사용 방지
        session.removeAttribute(SS_AUTH_EMAIL);
        session.removeAttribute(SS_AUTH_EXPIRE);
        return null;
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

            return exists ? msg(0, "이미 사용 중인 아이디입니다.")
                          : msg(1, "사용할 수 있는 아이디입니다.");
        } catch (Exception e) {
            log.error("checkLoginIdProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
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
            String codeError = verifyAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError);
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setName(userName);
            // 비밀번호는 절대 복호화되지 않도록 해시로만 저장한다.
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));
            // 민감정보인 이메일은 AES-128-CBC 로 암호화해 저장한다.
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            // 필수 약관 3종은 1단계(signup-terms)를 통과해야 여기 올 수 있으므로 동의로 기록한다.
            // 선택 항목은 아직 값을 넘겨받는 화면이 없어 DB 기본값과 같은 값을 그대로 쓴다.
            // TODO signup-terms 에서 실제 체크값(특히 agree_marketing)을 넘겨받도록 바꿀 것.
            pDTO.setAgreeService(1);
            pDTO.setAgreePrivacy(1);
            pDTO.setAgreeSensitive(1);
            pDTO.setNotifyWeeklyReport(1);
            pDTO.setNotifyReminder(1);
            pDTO.setAgreeMarketing(0);

            // 마지막 방어선 — 화면에서 중복 확인을 건너뛰고 바로 쏠 수 있다.
            if ("Y".equals(CmmUtil.nvl(userService.getLoginIdExists(pDTO).getExistsYn()))) {
                return msg(0, "이미 사용 중인 아이디입니다.");
            }
            if ("Y".equals(CmmUtil.nvl(userService.getEmailExists(pDTO).getExistsYn()))) {
                return msg(0, "이미 가입된 이메일입니다.");
            }

            return userService.insertUser(pDTO) == 1
                    ? msg(1, "회원가입이 완료되었습니다.")
                    : msg(0, "회원가입에 실패했습니다.");

        } catch (Exception e) {
            log.error("signupProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
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
            String codeError = verifyAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError);
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setName(userName);
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            UserDTO rDTO = userService.getFindId(pDTO);

            if (rDTO == null) {
                return msg(0, "일치하는 회원 정보가 없습니다.");
            }

            // 메일 인증을 통과한 본인이므로 아이디를 가리지 않고 그대로 보여 준다.
            // 결과는 세션으로 넘긴다 — 주소창에 실으면 방문 기록·리퍼러에 남는다.
            session.setAttribute(SS_FOUND_ID, CmmUtil.nvl(rDTO.getLoginId()));

            return msg(1, "아이디를 찾았습니다.");

        } catch (Exception e) {
            log.error("findIdProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    /* ================================================================
     * 비밀번호 찾기 — 1) 메일 인증  2) 새 비밀번호 저장
     * ================================================================ */

    /** 1단계: 메일 인증이 끝나면 재설정 화면으로 갈 수 있는 표를 세션에 끊어 준다. */
    @PostMapping("/findPwProc")
    public MsgDTO findPwProc(@RequestParam String email,
                             @RequestParam String authCode,
                             HttpSession session) {
        try {
            String codeError = verifyAuthCode(email, authCode, session);
            if (codeError != null) {
                return msg(0, codeError);
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));

            UserDTO rDTO = userService.getFindPwUser(pDTO);

            if (rDTO == null) {
                return msg(0, "일치하는 회원 정보가 없습니다.");
            }

            // 이 표가 있어야만 새 비밀번호를 저장할 수 있다(주소만 쳐서 들어오는 것 차단).
            session.setAttribute(SS_PW_RESET_ID, rDTO.getLoginId());

            return msg(1, "본인 확인이 완료되었습니다.");

        } catch (Exception e) {
            log.error("findPwProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    /** 2단계: 새 비밀번호 저장. 세션의 표를 쓰고 즉시 지운다. */
    @PostMapping("/newPasswordProc")
    public MsgDTO newPasswordProc(@RequestParam String newPassword, HttpSession session) {
        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_PW_RESET_ID));

            if (loginId.isEmpty()) {
                return msg(0, "비정상적인 접근입니다. 처음부터 다시 진행해 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setPassword(EncryptUtil.encHashSHA256(newPassword));

            if (userService.newPasswordProc(pDTO) < 1) {
                return msg(0, "비밀번호 변경에 실패했습니다.");
            }

            session.removeAttribute(SS_PW_RESET_ID);   // 한 번 쓰면 폐기

            return msg(1, "비밀번호가 변경되었습니다.");

        } catch (Exception e) {
            log.error("newPasswordProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }
}
