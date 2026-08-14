package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

// 스토리 노드 6단계
public enum StoryStage {

    // 상황 이야기
    STORY(1, "이야기", "scene", null),

    // 마음 읽기
    MIND(2, "마음", "feel", MissionType.CHOICE),

    // 이유 찾기
    CAUSE(3, "왜?", "why", MissionType.CHOICE),

    // 표정 따라하기
    EXPRESSION(4, "표정", "face", MissionType.EXPRESSION),

    // 동작 따라하기
    ACTION(5, "행동", "act", MissionType.GESTURE),

    // 칭찬
    PRAISE(6, "칭찬", "result", null);

    private final int seq;
    private final String label;
    private final String screenKey;
    private final MissionType defaultMission;

    StoryStage(int seq, String label, String screenKey, MissionType defaultMission) {
        this.seq = seq;
        this.label = label;
        this.screenKey = screenKey;
        this.defaultMission = defaultMission;
    }

    public int seq() {
        return seq;
    }
    public String label() {
        return label;
    }
    public String screenKey() {
        return screenKey;
    }

    public MissionType defaultMission() {
        return defaultMission;
    }
    public boolean hasMission() {
        return defaultMission != null;
    }

    public Optional<StoryStage> next() {
        StoryStage[] all = values();
        return ordinal() + 1 < all.length ? Optional.of(all[ordinal() + 1]) : Optional.empty();
    }

    public static StoryStage first() {
        return STORY;
    }

    public static Optional<StoryStage> ofSeq(int seq) {
        return Arrays.stream(values()).filter(s -> s.seq == seq).findFirst();
    }

    public static Optional<StoryStage> of(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String v = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(v) || s.screenKey.equalsIgnoreCase(v))
                .findFirst();
    }
}
