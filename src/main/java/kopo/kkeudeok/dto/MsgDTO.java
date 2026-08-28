package kopo.kkeudeok.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MsgDTO {

    private int result;

    private String msg;

    private String field;

    private String next;

    private int lockLeft;
}
