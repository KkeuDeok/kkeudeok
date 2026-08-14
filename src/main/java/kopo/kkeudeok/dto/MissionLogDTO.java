package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 미션 수행 결과
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionLogDTO {

    private Long logId;
    private Long sessionId;
    private Long nodeId;
    private String missionType;
    private String targetValue;
    // 아이의 실제 반응값
    private String responseValue;
    private Boolean isSuccess;
    private LocalDateTime createdAt;

}
