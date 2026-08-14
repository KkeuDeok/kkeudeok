package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

// 사회적 상황 분류
public enum SituationType {

    WAIT_TURN("차례 기다리기"),
    SHARE("양보하기"),
    COMFORT("친구 위로하기"),
    APOLOGIZE("사과하기"),
    CELEBRATE("함께 기뻐하기"),
    ASK_HELP("도움 요청하기"),
    GREET("먼저 말 걸기"),
    EXPRESS("감정 표현하기");

    private final String label;

    SituationType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static SituationType normalize(String raw, String emotion) {

        if (raw != null && !raw.isBlank()) {
            String v = raw.trim();

            Optional<SituationType> found = Arrays.stream(values())
                    .filter(s -> s.label.equals(v) || s.name().equalsIgnoreCase(v))
                    .findFirst();

            if (found.isPresent()) {
                return found.get();
            }

            Optional<SituationType> loose = Arrays.stream(values())
                    .filter(s -> v.contains(s.label.substring(0, 2)))
                    .findFirst();

            if (loose.isPresent()) {
                return loose.get();
            }
        }

        return defaultFor(emotion);
    }

    // 감정별 기본 상황
    public static SituationType defaultFor(String emotion) {
        return switch (emotion == null ? "" : emotion) {
            case "angry" -> APOLOGIZE;
            case "happy" -> CELEBRATE;
            case "surprise" -> EXPRESS;
            default -> COMFORT;
        };
    }

    // 프롬프트에 넣을 목록
    public static String promptList() {
        return String.join(" | ", Arrays.stream(values()).map(SituationType::label).toList());
    }
}
