package kopo.kkeudeok.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MsgDTO {

    private int result; // 처리 결과에 대한 상태 코드 | 0 : 실패 | 1: 성공 | 2 : 시스템 오류

    private String msg;
}
