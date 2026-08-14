package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 표정 캘리브레이션
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionCalibDTO {

    private Long calibId;
    private Long childId;
    private String emotionType;

    // 표정 특징값 JSON
    private String landmarkData;

    // 원본 이미지 경로
    private String mediaUrl;

    private LocalDateTime createdAt;
}
