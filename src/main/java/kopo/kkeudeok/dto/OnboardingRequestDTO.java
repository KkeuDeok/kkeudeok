package kopo.kkeudeok.dto;

import lombok.Data;

import java.util.List;

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

    private List<Answer> checklist;

    @Data
    public static class Answer {

        private String domain;
        private Integer questionNo;
        private Integer score;

    }
}
