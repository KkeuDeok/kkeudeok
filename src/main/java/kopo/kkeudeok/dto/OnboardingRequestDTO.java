package kopo.kkeudeok.dto;

import lombok.Data;

import java.util.List;

// 온보딩 값
@Data
public class OnboardingRequestDTO {

    private String name;
    private Integer birthY;
    private Integer birthM;
    private Integer birthD;
    private String gender;
    private String disType;
    private String disLevel;
    private String charKey;
    private String nickname;

    // 발달 체크리스트 응답 12개
    private List<Answer> checklist;

    @Data
    public static class Answer {

        private String domain;
        private Integer questionNo;
        private Integer score;

    }
}
