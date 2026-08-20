package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 시나리오 (세션을 시작할 때 AI 가 한 번에 만드는 이야기의 뼈대)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoryScenarioDTO {

    private String title;
    private String emotion;
    private String situationType;
    private String situation;
    private String recap;
    private String cause;
    private String causeDistractor;
    private String gesture;
    private String praise;
    private String source;

}
