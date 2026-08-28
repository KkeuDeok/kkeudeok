package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

public enum MissionType {

    EXPRESSION("표정"),
    GESTURE("동작"),
    VOICE("음성"),
    CHOICE("선택");

    private final String label;

    MissionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Optional<MissionType> of(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String v = value.trim();
        return Arrays.stream(values()).filter(m -> m.name().equalsIgnoreCase(v)).findFirst();
    }
}
