package kopo.kkeudeok.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 아이를 뭐라고 부르는지.
 *
 * <p>화면 곳곳에서 아이 이름 뒤에 조사가 붙는다. 여기가 어긋나면 "이진이야!" 처럼
 * 어색하게 불리는데, 이야기 화면은 그 말을 소리 내어 읽어 주기까지 한다.
 * 실제로 두 번 지적받은 자리라(2026-08-18) 규칙을 못 박아 둔다.
 */
class ChildDTONameTest {

    private static ChildDTO of(String name) {
        return ChildDTO.builder().name(name).build();
    }

    /**
     * 부를 때는 '이' 를 붙이지 않는다.
     *
     * <p>getCallName() 이 만든 '이진이' 에 야를 달아 "이진이야" 가 나온 게 원인이었다.
     * 부르는 말은 성을 뗀 이름에서 곧바로 만든다.
     */
    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("부를 때는 받침이 있으면 아, 없으면 야")
    @CsvSource({
            "이진,   이진아",     // 받침 ㄴ
            "김이진, 이진아",     // 성을 뗀다
            "지우,   지우야",     // 받침 없음
            "박지우, 지우야",
            "남궁민서, 민서야",   // 두 글자 성
            "은,     은아"        // 외자
    })
    void vocativeUsesGivenNameWithoutSuffix(String name, String expected) {
        assertThat(of(name).getVocative()).isEqualTo(expected);
    }

    /** 가리킬 때는 '이진이의 성장 리포트' 처럼 이를 붙인다 — 부르는 말과 다른 규칙이다. */
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
