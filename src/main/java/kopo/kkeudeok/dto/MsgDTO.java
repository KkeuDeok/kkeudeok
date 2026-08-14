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

    private String next;

    /**
     * 남은 잠금 시간(초). 0 이면 안 잠긴 상태다.
     * 보호자 PIN 게이트만 쓴다 — 화면이 이 숫자로 카운트다운을 그린다.
     * 남은 시간을 화면이 계산하면 시계를 돌려 풀 수 있으므로 서버가 준 값만 쓴다.
     */
    private int lockLeft;
}
