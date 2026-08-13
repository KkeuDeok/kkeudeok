package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.MailDTO;

import java.util.List;

public interface IMailService {
    //메일 발송
    int doSendMail(MailDTO pDTO);
}
