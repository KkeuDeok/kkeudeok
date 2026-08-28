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
public class ExpressionCalibDTO {

    private Long calibId;
    private Long childId;
    private String emotionType;

    private String landmarkData;

    private String mediaUrl;

    private LocalDateTime createdAt;
}
