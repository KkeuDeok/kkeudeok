package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

public enum EmotionType {

    HAPPY("happy", "기쁨"),
    SAD("sad", "슬픔"),
    ANGRY("angry", "화남"),
    SURPRISED("surprise", "놀람"),
    NEUTRAL("neutral", "무표정");

    private final String screenKey;
    private final String label;

    EmotionType(String screenKey, String label) {
        this.screenKey = screenKey;
        this.label = label;
    }

    public String screenKey() {
        return screenKey;
    }
    public String label() {
        return label;
    }

    public static Optional<EmotionType> of(String value) {

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String v = value.trim();

        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(v)
                        || e.screenKey.equalsIgnoreCase(v)
                        || e.label.equals(v))
                .findFirst();
    }
}
