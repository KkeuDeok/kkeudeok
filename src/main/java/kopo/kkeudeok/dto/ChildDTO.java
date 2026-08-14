package kopo.kkeudeok.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    public Integer getAge() {
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }
    public String getCallName() {
        String n = name == null ? "" : name.trim();

        if (n.length() >= 3) {
            // 두 글자 성은 따로 안다 — 남궁·황보처럼 성이 두 글자면 앞 두 글자를 뗀다
            String[] surname2 = {"남궁", "황보", "제갈", "사공", "선우", "서문", "독고", "동방"};
            String head = n.substring(0, 2);
            boolean two = false;
            for (String s : surname2) {
                if (s.equals(head)) {
                    two = true;
                    break;
                }
            }
            n = two ? n.substring(2) : n.substring(1);
        }

        if (n.isEmpty()) {
            return "";
        }

        int code = n.charAt(n.length() - 1) - 0xAC00;
        boolean hasJongseong = code >= 0 && code < 11172 && code % 28 != 0;

        return hasJongseong ? n + "이" : n;
    }
}
