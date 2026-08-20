package kopo.kkeudeok.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 이야기 뼈대를 화면 문구로 옮길 때의 규칙.
 *
 * <p>여기서 정한 값이 곧 인식 목표가 되므로, 알아들을 수 없는 값이 새어 나가면
 * 아이가 미션을 통과할 방법이 사라진다.
 */
class StoryTemplateTest {

    /**
     * AI 가 엉뚱한 동작 이름을 주면 kd-mediapipe.js 가 영영 맞히지 못한다.
     * [다 했어요!] 로 넘어가는 길을 없앤 뒤로는 그대로 화면에 갇히므로 반드시 걸러야 한다.
     */
    @Test
    @DisplayName("인식할 수 없는 동작은 감정에 맞는 기본 동작으로 바꾼다")
    void unknownGestureFallsBack() {

        assertThat(StoryTemplate.gesture("hug", "sad")).isEqualTo("comfort");
        assertThat(StoryTemplate.gesture("토닥토닥", "sad")).isEqualTo("comfort");
        assertThat(StoryTemplate.gesture(null, "angry")).isEqualTo("sorry");
        assertThat(StoryTemplate.gesture("", "happy")).isEqualTo("celebrate");
        assertThat(StoryTemplate.gesture("hug", "surprise")).isEqualTo("comfort");
    }

    @Test
    @DisplayName("알아들을 수 있는 동작은 그대로 쓴다")
    void knownGestureKept() {

        assertThat(StoryTemplate.gesture("celebrate", "sad")).isEqualTo("celebrate");
        assertThat(StoryTemplate.gesture("  Sorry  ", "happy")).isEqualTo("sorry");
    }

    /* ---------- 아이 이름 다듬기 ----------
       모델은 부르는 형태("이진아")를 이름으로 알고 조사를 이어 붙인다.
       화면에 "이진아와 토리가 기뻐해요" 가 뜬 적이 있다(2026-08-20 지적). */

    /** 받침 있는 이름 — 이진 / 이진이 / 이진아 */
    private static String jong(String text) {
        return StoryTemplate.fixChildName(text, "이진", "이진이", "이진아");
    }

    /** 받침 없는 이름 — 지우 / 지우 / 지우야 */
    private static String noJong(String text) {
        return StoryTemplate.fixChildName(text, "지우", "지우", "지우야");
    }

    @Test
    @DisplayName("부르는 형태에 조사가 붙으면 가리키는 형태로 되돌린다")
    void vocativeBecomesCallName() {

        assertThat(jong("이진아와 토리가 기뻐해요")).isEqualTo("이진이와 토리가 기뻐해요");
        assertThat(jong("이진아가 웃었어요")).isEqualTo("이진이가 웃었어요");
        assertThat(noJong("지우야와 토리가 놀아요")).isEqualTo("지우와 토리가 놀아요");
    }

    @Test
    @DisplayName("이름만 쓴 것도 부르는 말에 맞춘다")
    void bareNameGetsSuffix() {

        assertThat(jong("이진은 토리를 도왔어요")).isEqualTo("이진이는 토리를 도왔어요");
        assertThat(jong("토리와 이진을 보았어요")).isEqualTo("토리와 이진이를 보았어요");
    }

    /** callName 은 받침이 없으므로 그 뒤 조사도 언제나 받침 없는 쪽이다 */
    @Test
    @DisplayName("이름 뒤 조사를 받침 없는 쪽으로 맞춘다")
    void particlesFollowCallName() {

        assertThat(jong("이진이은 기뻐요")).isEqualTo("이진이는 기뻐요");
        assertThat(jong("이진이과 토리")).isEqualTo("이진이와 토리");
        assertThat(jong("이진이이랑 토리")).isEqualTo("이진이랑 토리");
    }

    @Test
    @DisplayName("이미 자연스러운 문장은 건드리지 않는다")
    void naturalTextUntouched() {

        assertThat(jong("이진이가 토리를 토닥였어요")).isEqualTo("이진이가 토리를 토닥였어요");
        assertThat(noJong("지우가 손을 흔들었어요")).isEqualTo("지우가 손을 흔들었어요");
        assertThat(jong(null)).isNull();
    }
}
