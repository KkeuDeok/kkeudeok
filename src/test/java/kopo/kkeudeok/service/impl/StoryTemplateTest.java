package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.CharacterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoryTemplateTest {
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

    private static String jong(String text) {
        return StoryTemplate.fixChildName(text, "이진", "이진이", "이진아");
    }

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

    @Test
    @DisplayName("애칭을 정했으면 그것이 이름이다")
    void nicknameWins() {

        assertThat(StoryTemplate.charName("koko", "스팸")).isEqualTo("스팸");
        assertThat(StoryTemplate.charName("koko", "  스팸  ")).isEqualTo("스팸");
    }

    @Test
    @DisplayName("애칭이 없으면 고른 캐릭터의 기본 이름을 쓴다")
    void fallsBackToCharacterLabel() {

        assertThat(StoryTemplate.charName("koko", null)).isEqualTo("코코");
        assertThat(StoryTemplate.charName("rubi", "")).isEqualTo("루비");
        assertThat(StoryTemplate.charName("코코", null)).isEqualTo("코코");
        assertThat(StoryTemplate.charName("없는키", null)).isEqualTo("토리");
        assertThat(StoryTemplate.charName(null, null)).isEqualTo("토리");
    }

    @Test
    @DisplayName("캐릭터 6종이 모두 같은 이름표를 가리킨다")
    void everyCharacterResolves() {

        for (CharacterType c : CharacterType.values()) {
            assertThat(StoryTemplate.charName(c.key(), null))
                    .as("%s 의 이름", c.key())
                    .isEqualTo(c.label())
                    .isEqualTo(CharacterType.labelOf(c.key()));
        }
    }

    @Test
    @DisplayName("받침 있는 애칭 뒤 조사를 받침 있는 쪽으로 맞춘다")
    void friendParticlesFollowNickname() {

        assertThat(StoryTemplate.fixJosa("스팸 가 놀랐어요", "스팸")).isEqualTo("스팸이 놀랐어요");
        assertThat(StoryTemplate.fixJosa("스팸는 웃었어요", "스팸")).isEqualTo("스팸은 웃었어요");
        assertThat(StoryTemplate.fixJosa("스팸를 도와줄래?", "스팸")).isEqualTo("스팸을 도와줄래?");
        assertThat(StoryTemplate.fixJosa("스팸 와 함께", "스팸")).isEqualTo("스팸과 함께");
        assertThat(StoryTemplate.fixJosa("스팸 에게 갔어요", "스팸")).isEqualTo("스팸에게 갔어요");
    }
    @Test
    @DisplayName("받침 없는 애칭은 받침 없는 쪽을 쓴다")
    void friendParticlesWithoutJong() {

        assertThat(StoryTemplate.fixJosa("토리 이 웃어요", "토리")).isEqualTo("토리가 웃어요");
        assertThat(StoryTemplate.fixJosa("토리을 도와줄래?", "토리")).isEqualTo("토리를 도와줄래?");
        assertThat(StoryTemplate.fixJosa("토리가 웃어요", "토리")).isEqualTo("토리가 웃어요");
    }

    @Test
    @DisplayName("애칭이 아닌 말 뒤에 띄어 쓴 조사도 붙인다")
    void looseParticlesAreTightened() {

        assertThat(StoryTemplate.tightenParticles("문구점 에서 만났어요"))
                .isEqualTo("문구점에서 만났어요");
        assertThat(StoryTemplate.tightenParticles("장난감 을 보았어요"))
                .isEqualTo("장난감을 보았어요");
    }

    @Test
    @DisplayName("조사처럼 생긴 낱말은 붙이지 않는다")
    void verbsAreNotTightened() {

        assertThat(StoryTemplate.tightenParticles("토리에게 가 보자")).isEqualTo("토리에게 가 보자");
        assertThat(StoryTemplate.tightenParticles("이리 와 볼래?")).isEqualTo("이리 와 볼래?");
        assertThat(StoryTemplate.tightenParticles("같이 가 줄래?")).isEqualTo("같이 가 줄래?");
    }
}
