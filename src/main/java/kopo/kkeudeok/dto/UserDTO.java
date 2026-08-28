package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long memberId;
    private String loginId;
    private String password;
    private String parentPin;
    private String name;
    private String email;
    private String phone;
    private String relation;

    private Integer agreeService;
    private Integer agreePrivacy;
    private Integer agreeSensitive;
    private LocalDateTime agreedAt;

    private Integer notifyWeeklyReport;
    private Integer notifyReminder;
    private Integer agreeMarketing;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String existsYn;
}
