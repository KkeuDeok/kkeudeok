package kopo.kkeudeok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @EnableAsync — 메일 발송을 요청 스레드에서 떼어 내기 위한 것.
 * SMTP 접속·전송에 1~3초가 걸리는데, 그동안 화면이 응답을 기다리면
 * [인증번호 전송]을 눌러도 입력칸이 한참 뒤에 뜬다(MailService 참고).
 */
@EnableAsync
@SpringBootApplication
public class KkeudeokApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkeudeokApplication.class, args);
    }

}
