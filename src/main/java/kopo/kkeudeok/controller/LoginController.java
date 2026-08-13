package kopo.kkeudeok.controller;

import java.security.SecureRandom;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.service.impl.MailService;
import kopo.kkeudeok.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final IUserService userService;
    private final MailService mailService;

    /** 인증번호 관련 세션 키 — SS_ 는 Session 을 뜻하는 강의 규칙. */
    private static final String SS_AUTH_CODE = "SS_AUTH_CODE";
    private static final String SS_AUTH_EMAIL = "SS_AUTH_EMAIL";
    private static final String SS_AUTH_EXPIRE = "SS_AUTH_EXPIRE";

    /** 인증번호 유효시간 3분 — 화면 타이머(180초)와 같은 값이어야 한다. */
    private static final long CODE_VALID_MS = 3 * 60 * 1000L;

    /** MsgDTO 를 만드는 짧은 도우미. new + set 2줄이 계속 반복돼서 뺐다. */
    private MsgDTO msg(int result, String text) {
        MsgDTO dto = new MsgDTO();
        dto.setResult(result);
        dto.setMsg(text);
        return dto;
    }

    @PostMapping("/loginProc")
    public MsgDTO loginProc(@RequestParam String loginId,
                            @RequestParam String password,
                            HttpSession session) {

        // loginProc 이 loginiD 와 password 와 session을 처리하고 ->  MsgDTO에 담아서 준다

        log.info("{}.loginProc Start!", // 1. 실행 시작 로그 남기기
                this.getClass().getName());

        try {
            UserDTO pDTO = new UserDTO(); //
            pDTO.setLoginId(loginId); // 여기 안에 logId 넣어요. 이거를 볼거면 => IUserService를 확인, 얘는 loginId를 요구함
            pDTO.setPassword(EncryptUtil.encHashSHA256(password)); // 평문을 -> 해시로 만든다 암호화

            UserDTO rDTO = userService.getLogin(pDTO); // 서비스를 getlogin을 호출하고 -> pDTO에 넣는다

            MsgDTO msgDTO = new MsgDTO();

            if (rDTO == null) {
                msgDTO.setResult(0); //추측하기로아까 MsgDTO 에서 0이나 1 반환하는거 있었는데 그게 이건듯
                msgDTO.setMsg("아이디 또는 비밀번호를 확인해주세요.");

            } else {
                session.setAttribute("SS_USER_ID", rDTO.getLoginId()); //세션 ID 와 이름만 저자ㅓㅇ
                session.setAttribute("SS_USER_NAME", rDTO.getName()); // 쿠키 : 로그인 하면 계속 로그인하게끔.

                msgDTO.setResult(1);
                msgDTO.setMsg("환영합니다.");
            }

            return msgDTO;
        } catch (Exception e) {
            log.error("loginProc 실패", e);

            MsgDTO msgDTO = new MsgDTO();
            msgDTO.setResult(2);
            msgDTO.setMsg("시스템 오류가 발생했습니다.");
            return msgDTO;
        }
    }

    /** 인증번호를 만들어 메일로 보내고, 정답은 세션에 적어 둔다. */
    @PostMapping("/sendAuthCodeProc")
    public MsgDTO sendAuthCodeProc(@RequestParam String email, HttpSession session) {
        try {
            // 000000 ~ 999999 중 하나. 앞자리가 0이어도 6자리가 되도록 %06d 로 채운다.
            String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));

            mailService.sendAuthCode(email, code);

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

        session.removeAttribute(SS_AUTH_CODE);      // 한 번 쓰면 폐기 - 재사용 방지
        session.removeAttribute(SS_AUTH_EMAIL);
        session.removeAttribute(SS_AUTH_EXPIRE);
        return null;                                 // null = 통과
    }

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
            pDTO.setEmail(email);

            UserDTO rDTO = userService.getFindId(pDTO);

            MsgDTO msgDTO = new MsgDTO();

            if (rDTO == null) {
                msgDTO.setResult(0);
                msgDTO.setMsg("일치하는 회원 정보가 없습니다.");
            } else {
                msgDTO.setResult(1);
                msgDTO.setMsg(maskLoginId(rDTO.getLoginId()));   // "pa1234" -> "pa****"
            } // *** 없애기

            return msgDTO;

        } catch (Exception e) {
            log.error("findIdProc 실패", e);
            MsgDTO msgDTO = new MsgDTO();
            msgDTO.setResult(2);
            msgDTO.setMsg("시스템 오류가 발생했습니다.");
            return msgDTO;
        }
    }

    private String maskLoginId(String loginId) {
        if (loginId.length() <= 2) return loginId;
        return loginId.substring(0, 2) + "*".repeat(loginId.length() - 2);
    }
}
