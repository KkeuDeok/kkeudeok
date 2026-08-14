package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.MailDTO;

public interface IMailService {

    int doSendMail(MailDTO pDTO);

    void sendAuthCode(String toEmail, String code);
}
