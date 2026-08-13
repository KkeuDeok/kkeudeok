package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * member 테이블 1행.
 *
 * 저장 규칙 — 화면에서 받은 값을 그대로 넣지 않는다.
 *  - password : EncryptUtil.encHashSHA256 (단방향, 복호화 불가)
 *  - email    : EncryptUtil.encAES128CBC (양방향, 메일 보낼 때 복호화)
 * 조회할 때도 같은 방식으로 암호화한 값으로 WHERE 를 걸어야 맞는다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    /** 회원 고유 번호 */
    private Long memberId;
    /** 아이디 */
    private String loginId;
    /** 비밀번호(해시) */
    private String password;
    /** 보호자 PIN */
    private String parentPin;
    /** 이름 */
    private String name;
    /** 이메일(AES 암호문) */
    private String email;
    /** 연락처 */
    private String phone;
    /** 아동과의 관계 */
    private String relation;

    /* 약관 동의 */
    private Integer agreeService;
    private Integer agreePrivacy;
    private Integer agreeSensitive;
    private LocalDateTime agreedAt;

    /* 알림 설정 */
    private Integer notifyWeeklyReport;
    private Integer notifyReminder;
    private Integer agreeMarketing;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /* ---------- DB 컬럼이 아닌 값 ---------- */

    /** 중복 조회 결과 — 존재하면 "Y", 없으면 "N" */
    private String existsYn;
}
