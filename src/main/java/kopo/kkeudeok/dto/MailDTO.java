package kopo.kkeudeok.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MailDTO {

    String toMail;
    String title;
    String contents;
}
