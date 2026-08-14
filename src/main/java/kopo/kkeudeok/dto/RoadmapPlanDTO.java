package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 로드맵 주차 계획
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadmapPlanDTO {

    // 형식
    @Builder.Default
    private int version = 1;

    // 전체 주차 수
    @Builder.Default
    private int totalWeeks = 12;

    // 단계
    @Builder.Default
    private List<Stage> stages = new ArrayList<>();

    // 주차별 주제
    @Builder.Default
    private List<Week> weeks = new ArrayList<>();

    /**
     * 이 계획이 어디서 왔는가 — {@code AI} | {@code STANDARD}.
     *
     * 서비스가 이 값으로 roadmap_type 을 정한다. 생성한 쪽만 아는 정보라 계획 자신이 들고 온다(추측하지 않는다)
     */
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

    // 주차 하나
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Week {

        private int no;

        // 속한 단계 이름
        private String stage;

        // 프론트에 뜨는 주제 한 줄
        private String topic;

        // 사회적 상황
        private String situationType;

        // 보호자에게 보여 줄 이번 주 목표 한 문장
        private String goal;
    }
}
