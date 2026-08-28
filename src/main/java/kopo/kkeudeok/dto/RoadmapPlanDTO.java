package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadmapPlanDTO {

    @Builder.Default
    private int version = 1;

    @Builder.Default
    private int totalWeeks = 12;

    @Builder.Default
    private List<Stage> stages = new ArrayList<>();

    @Builder.Default
    private List<Week> weeks = new ArrayList<>();
    private String source;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stage {
        private String name;
        private int weeks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Week {

        private int no;

        private String stage;

        private String topic;

        private String situationType;

        private String goal;
    }
}
