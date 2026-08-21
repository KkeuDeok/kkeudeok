package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 이야기
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDTO {

    private Long storyId;
    private Long childId;
    private String title;
    private String situationType;
    private String emotion;
    private Boolean isGenerated;
}
