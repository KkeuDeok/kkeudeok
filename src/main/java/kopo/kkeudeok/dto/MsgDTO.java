package kopo.kkeudeok.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 화면(JS)으로 돌려보내는 처리 결과.
 *
 * ⚠ @JsonInclude(NON_DEFAULT) 를 붙이지 말 것. result 의 기본값이 0(=실패)이라
 *    실패 응답에서 result 필드가 통째로 빠져 JS 의 data.result 가 undefined 가 된다.
 */
@Setter
@Getter
public class MsgDTO {

    /** 0 : 실패 | 1 : 성공 | 2 : 시스템 오류 */
    private int result;

    /** 화면에 그대로 보여 줄 메시지 */
    private String msg;
}
