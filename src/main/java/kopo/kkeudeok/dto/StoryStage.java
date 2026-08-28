package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

public enum StoryStage {

    STORY(1, "이야기", "scene", null),

    MIND(2, "마음", "feel", MissionType.CHOICE),

    CAUSE(3, "왜?", "why", MissionType.CHOICE),

    EXPRESSION(4, "표정", "face", MissionType.EXPRESSION),

    ACTION(5, "행동", "act", MissionType.GESTURE),

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
