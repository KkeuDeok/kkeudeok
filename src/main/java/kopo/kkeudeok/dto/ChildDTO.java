package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

// 아동
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDTO {

    private Long childId;
    private Long memberId;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String disorderType;
    private String severity;
    private String characterType;
    private String characterNickname;
    private LocalDateTime createdAt;

    public Integer getAge() {
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }

    // 이 서비스가 상대하는 나이 (유치원·어린이집부터 초등 저학년까지)
    private static final int MIN_AGE = 3;
    private static final int MAX_AGE = 12;

    public String promptAge() {

        Integer age = getAge();

        if (age == null || age < MIN_AGE || age > MAX_AGE) {
            return "6세 정도";
        }

        return age + "세";
    }
    private String givenName() {

        String n = name == null ? "" : name.trim();

        if (n.length() >= 3) {
            String[] surname2 = {"남궁", "황보", "제갈", "사공", "선우", "서문", "독고", "동방"};
            String head = n.substring(0, 2);
            boolean two = false;
            for (String x : surname2) {
                if (x.equals(head)) {
                    two = true;
                    break;
                }
            }
            n = two ? n.substring(2) : n.substring(1);
        }

        return n;
    }

    private static boolean hasJong(String n) {
        if (n == null || n.isEmpty()) {
            return false;
        }
        int code = n.charAt(n.length() - 1) - 0xAC00;
        return code >= 0 && code < 11172 && code % 28 != 0;
    }

    public String getCallName() {
        String n = givenName();
        return n.isEmpty() ? "" : (hasJong(n) ? n + "이" : n);
    }

    public String getVocative() {
        String n = givenName();
        return n.isEmpty() ? "" : n + (hasJong(n) ? "아" : "야");
    }

    public String getGivenName() {
        return givenName();
    }
}
