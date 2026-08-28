package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String responseValue;
    private Boolean isSuccess;
    private LocalDateTime createdAt;
    private String stageType;
    private LocalDateTime startedAt;
}
