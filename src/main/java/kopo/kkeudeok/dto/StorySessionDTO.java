package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 학습 세션
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySessionDTO {

    public static final String COMPLETED = "COMPLETED";
    public static final String INCOMPLETE = "INCOMPLETE";

    private Long sessionId;
    private Long childId;
    private Long storyId;
    private Long roadmapId;
    private String dailyInput;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private String status;
    private boolean prepared;
    private String storyTitle;

}
