package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryNodeDTO {

    private Long nodeId;

    @JsonIgnore
    private Long storyId;

    private Integer nodeOrder;
    private String stageType;
    private String narration;
    private String questionText;

    @JsonIgnore
    private String choiceData;

    private String title;
    private String missionType;
    private String targetValue;
    private String coachText;
    private String hintText;
    private String charPose;

    @Builder.Default
    private List<StoryOptionDTO> options = new ArrayList<>();

    @JsonIgnore
    private StoryScenarioDTO scenario;

    @JsonIgnore
    public boolean hasMission() {
        return missionType != null && !missionType.isBlank();
    }
}
