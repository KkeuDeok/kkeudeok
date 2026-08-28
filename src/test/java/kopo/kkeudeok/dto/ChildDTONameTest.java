package kopo.kkeudeok.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ChildDTONameTest {
    private static ChildDTO of(String name) {
        return ChildDTO.builder().name(name).build();
    }

    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("부를 때는 받침이 있으면 아, 없으면 야")
    @CsvSource({
            "이진,   이진아",
            "김이진, 이진아",
            "지우,   지우야",
            "박지우, 지우야",
            "남궁민서, 민서야",
            "은,     은아"
    })
    void vocativeUsesGivenNameWithoutSuffix(String name, String expected) {
        assertThat(of(name).getVocative()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("가리킬 때는 받침이 있으면 이를 붙인다")
    @CsvSource({
            "이진,   이진이",
            "김이진, 이진이",
            "지우,   지우",
            "남궁민서, 민서"
    })
    void callNameAddsSuffixWhenJongseong(String name, String expected) {
        assertThat(of(name).getCallName()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이름이 없으면 빈 문자열 — 조사만 덩그러니 남지 않는다")
    void emptyNameYieldsEmpty() {
        assertThat(of(null).getVocative()).isEmpty();
        assertThat(of("  ").getVocative()).isEmpty();
        assertThat(of(null).getCallName()).isEmpty();
    }
}
