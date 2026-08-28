package kopo.kkeudeok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KkeudeokApplication {
    public static void main(String[] args) {
        SpringApplication.run(KkeudeokApplication.class, args);
    }

}
