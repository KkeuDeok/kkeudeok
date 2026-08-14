package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 스토리 노드
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
    private String targetValue; // 정답값
    private String coachText; // 피드백 문구
    private String hintText; // 힌트
    private String charPose; // 캐릭터 포즈 키

    @Builder.Default
    private List<StoryOptionDTO> options = new ArrayList<>();

    @JsonIgnore
    private StoryScenarioDTO scenario;

    @JsonIgnore
    public boolean hasMission() {
        return missionType != null && !missionType.isBlank();
    }
}
