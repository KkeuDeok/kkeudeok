package kopo.kkeudeok.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MsgDTO {

    /** 0 : 실패 | 1 : 성공 | 2 : 시스템 오류 */
    private int result;

    /** 화면에 그대로 보여 줄 메시지 */
    private String msg;

    private String field;
}
