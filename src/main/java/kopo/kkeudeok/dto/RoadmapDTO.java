package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 학습 로드맵
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapDTO {

    public static final String TYPE_AI = "AI";
    public static final String TYPE_STANDARD = "STANDARD";

    private Long roadmapId;
    private Long childId;

    // AI(아이 맞춤) | STANDARD(정석 커리큘럼)
    private String roadmapType;

    @JsonIgnore
    private String stepData;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private RoadmapPlanDTO plan;

}
