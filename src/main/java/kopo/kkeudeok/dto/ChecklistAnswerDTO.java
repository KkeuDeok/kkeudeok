package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 온보딩 발달 체크리스트
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistAnswerDTO {

    private Long checklistId;
    private Long childId;
    private String domain;
    private Integer questionNo;
    private Integer score;
}
