package kopo.kkeudeok.service.impl;

import jakarta.mail.internet.MimeMessage;
import kopo.kkeudeok.dto.MailDTO;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromMail;

    @Override
    public int doSendMail(MailDTO pDTO) {
        log.info("{}.doSendMail Start!", this.getClass().getName());

        int res = 1;

        if (pDTO == null) {
            pDTO = new MailDTO();
        }

        String toMail = CmmUtil.nvl(pDTO.getToMail());
        String title = CmmUtil.nvl(pDTO.getTitle());
        String contents = CmmUtil.nvl(pDTO.getContents());

        log.info("toMail : {} / title : {}", toMail, title);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(toMail);
            helper.setFrom(fromMail);
            helper.setSubject(title);
            helper.setText(contents, true);

            mailSender.send(message);

        } catch (Exception e) {
            res = 0;
            log.error("[ERROR] doSendMail toMail={}", toMail, e);
        }

        log.info("{}.doSendMail End! res={}", this.getClass().getName(), res);
        return res;
    }

    @Async
    @Override
    public void sendWelcome(String toEmail, String name) {
        MailDTO dto = new MailDTO();
        dto.setToMail(toEmail);
        dto.setTitle("[끄덕] 회원가입을 축하합니다");
        dto.setContents(welcomeHtml(name));
        doSendMail(dto);
    }

    @Async
    @Override
    public void sendAuthCode(String toEmail, String code) {
        MailDTO dto = new MailDTO();
        dto.setToMail(toEmail);
        dto.setTitle("[끄덕] 인증번호 안내");
        dto.setContents(authCodeHtml(code));
        doSendMail(dto);
    }

    /* ====================================================================
     * 메일 템플릿
     * ==================================================================== */

    private static final String BRAND = "#3FB27F";

    private static String layout(String heading, String bodyHtml) {
        return """
                <div style="margin:0;padding:24px 12px;background:#F5F7F6;
                            font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;">
                  <table role="presentation" cellpadding="0" cellspacing="0" border="0"
                         style="width:100%%;max-width:480px;margin:0 auto;background:#FFFFFF;
                                border-radius:16px;overflow:hidden;">
                    <tr>
                      <td style="padding:28px 28px 8px;text-align:center;">
                        <div style="font-size:22px;font-weight:700;color:%s;">끄덕</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:8px 28px 0;text-align:center;">
                        <h1 style="margin:0;font-size:19px;line-height:1.5;color:#1F2A24;font-weight:700;">%s</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:16px 28px 28px;color:#4A5A52;font-size:15px;line-height:1.7;text-align:center;">
                        %s
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:16px 28px 24px;border-top:1px solid #ECEFED;
                                 color:#9AA8A1;font-size:12px;line-height:1.6;text-align:center;">
                        본 메일은 발신 전용입니다.<br>요청하지 않으셨다면 이 메일을 무시해 주세요.
                      </td>
                    </tr>
                  </table>
                </div>
                """.formatted(BRAND, heading, bodyHtml);
    }

    static String authCodeHtml(String code) {
        String body = """
                <p style="margin:0 0 18px;">아래 인증번호를 입력해 주세요.</p>
                <div style="display:inline-block;padding:14px 28px;background:#F1F8F5;
                            border:1px solid %s;border-radius:12px;
                            font-size:30px;font-weight:700;letter-spacing:8px;color:%s;">%s</div>
                <p style="margin:18px 0 0;font-size:13px;color:#7B8A83;">
                  인증번호는 <b>3분간</b> 유효합니다.
                </p>
                """.formatted(BRAND, BRAND, CmmUtil.nvl(code));

        return layout("인증번호를 입력해 주세요", body);
    }

    static String welcomeHtml(String name) {
        String body = """
                <p style="margin:0 0 8px;"><b>%s</b>님, 반가워요!</p>
                <p style="margin:0 0 20px;">끄덕과 함께 아이의 마음 읽기 연습을 시작해 보세요.</p>
                <a href="http://localhost:8080/login"
                   style="display:inline-block;padding:13px 30px;background:%s;color:#FFFFFF;
                          border-radius:999px;font-size:15px;font-weight:700;text-decoration:none;">
                  로그인하러 가기
                </a>
                """.formatted(CmmUtil.nvl(name), BRAND);

        return layout("회원가입이 완료되었어요", body);
    }
}
