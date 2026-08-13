package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.MailDTO;

public interface IMailService {

    /** 메일 발송 — 성공 1 / 실패 0. 내용은 HTML 로 보낸다. 호출한 자리에서 끝까지 기다린다. */
    int doSendMail(MailDTO pDTO);

    /**
     * 인증번호 안내 메일. 회원가입·아이디찾기·비밀번호찾기가 같이 쓴다.
     *
     * ⚠ 비동기다 — 부르면 바로 돌아오고 실제 발송은 뒤에서 끝난다.
     *   그래서 성공/실패를 돌려주지 못한다. 실패는 로그에만 남는다.
     *   화면을 SMTP 시간만큼 붙잡지 않으려고 이렇게 뒀다.
     */
    void sendAuthCode(String toEmail, String code);
}
