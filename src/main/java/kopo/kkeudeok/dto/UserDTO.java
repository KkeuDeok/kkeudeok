package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserDTO {
    //회원 고유 번호
    private Long memberId;
    //아이디
    private String loginId;
    //비번
    private String password;
    //보호자 핀 번호
    private String parentPin;
    //이름
    private String name;
    //메일
    private String email;
    //연락처
    private String phone;
    //아동과 관계
    private String relation;
    //약관 동의들
    private Integer agreeService;
    private Integer agreePrivacy;
    private Integer agreeSensitive;
    private LocalDateTime agreedAt;
    //알람 설정들
    private Integer notifyWeeklyReport;
    private Integer notifyReminder;
    private Integer agreeMarketing;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    //DB안들어가는거
    //DB를 조회해서 존재하면 y값 반환
    private String existsYn;
    // 이메일 중복체크를 위한 인증번호
    private int authNumber;
}
