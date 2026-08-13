package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserDTO { //이게 내가 알기로 데이터 베이스랑 연결하는 너는 이거 찾아라잉 이런 느낌 같음

    private long memberId;

    private String loginId;

    private String password;

    private String parentPin;

    private String name;

    private String email;

    private String phone;

    private String relation;

    private int agreeService;

    private int agreePrivacy;

    private int agreeSensitive;

    private LocalDateTime agreedAt;

    private int notifyWeeklyReport;

    private int notifyReminder;

    private int agreeMarketing;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;



}
