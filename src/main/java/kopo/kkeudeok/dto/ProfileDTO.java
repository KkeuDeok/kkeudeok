package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProfileDTO {
    private Long memberId;
    private Long childId;
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthDate;

    private String gender;
    private String disorderType;
    private String severity;
    private String characterType;
    private String characterNickname;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
