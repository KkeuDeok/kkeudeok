package kopo.kkeudeok.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** 인증번호 메일 발송. 회원가입·아이디찾기·비번찾기가 같이 쓴다. */
@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender mailSender;

    /** 보내는 사람 주소 = 로그인 계정과 같아야 네이버가 거부하지 않는다. */
    @Value("${spring.mail.username:}")
    private String from;

    public void sendAuthCode(String toEmail, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("[끄덕] 인증번호 안내");
        msg.setText("인증번호는 " + code + " 입니다.\n3분 안에 입력해 주세요.");

        mailSender.send(msg);
        log.info("인증번호 메일 발송 완료: {}", toEmail);
    }
}
