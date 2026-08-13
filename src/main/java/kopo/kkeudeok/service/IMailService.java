package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.MailDTO;

public interface IMailService {

    /** 메일 발송 — 성공 1 / 실패 0. 내용은 HTML 로 보낸다. */
    int doSendMail(MailDTO pDTO);

    /** 인증번호 안내 메일. 회원가입·아이디찾기·비밀번호찾기가 같이 쓴다. */
    int sendAuthCode(String toEmail, String code);
}
