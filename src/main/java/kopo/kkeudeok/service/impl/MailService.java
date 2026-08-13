package kopo.kkeudeok.service.impl;

import jakarta.mail.internet.MimeMessage;
import kopo.kkeudeok.dto.MailDTO;
import kopo.kkeudeok.service.IMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService implements IMailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public int doSendMail(MailDTO pDTO) {
        //로그 찍기(이후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이 하다.)
        log.info("{}.doSendMail start!", this.getClass().getName());
        //메일 발송 성공 여부(발송 성공 : 1 / 발송 실패 : 0)
        int res = 1;
        //전달 받은 DTO로부터 데이터 가져오기(DTO객체가 메모리에 올라가지 않아 Null이 발생할수 있기 떄문에
        //에러 방지 차원으로 if사용
        if (pDTO == null) {
            pDTO = new MailDTO();
        }
        String toMail = kopo.poly.util.CmmUtil.nvl(pDTO.getToMail()); //받는 사람
        String title = kopo.poly.util.CmmUtil.nvl(pDTO.getTitle()); //메일 제목
        String contents = kopo.poly.util.CmmUtil.nvl(pDTO.getContents()); //메일 제목? 내용 아님?
        log.info("toMail: {} / title : {} / contents : {}", toMail, title, contents);
        //메일 발송 메시지 구조 (파일 첨부 가능)
        MimeMessage message = mailSender.createMimeMessage();
        //메일 발송 메시지 구조를 쉽게 생성하게 도와주는 객체
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");
        try {
            messageHelper.setTo(toMail); //받는 사람
            messageHelper.setFrom(fromMail); //보내는 사람
            messageHelper.setSubject(title);// 메일 제목
            messageHelper.setText(contents); //메일 내용

            mailSender.send(message);
        } catch (Exception e) {//모든 에러 다 잡기
            res = 0; //메일 발송이 실패하기 때문에 0으로 변경
            log.info("[ERROR] doSendMail", e);
        }
        //로그 찍기(추후 찍은 로그를 통해 이 함수 호출이 끝났는지 파악하기 용이함)
        log.info("{}.doSendMail end!", this.getClass().getName());
        return res;
    }
}