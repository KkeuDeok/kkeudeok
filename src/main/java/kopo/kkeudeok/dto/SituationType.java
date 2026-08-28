package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public List<String> emotions() {
        return switch (this) {
            case CELEBRATE -> List.of("happy", "surprise", "sad");
            case GREET     -> List.of("happy", "sad", "surprise");
            case WAIT_TURN -> List.of("angry", "sad", "happy", "surprise");
            case APOLOGIZE -> List.of("angry", "sad", "surprise");
            case COMFORT   -> List.of("sad", "angry", "surprise");
            case SHARE     -> List.of("sad", "angry", "happy");
            case ASK_HELP  -> List.of("sad", "surprise", "angry");
            case EXPRESS   -> List.of("sad", "happy", "angry", "surprise");
        };
    }

    public String emotionFor(int seq) {
        List<String> all = emotions();
        return all.get(Math.floorMod(seq, all.size()));
    }

    public static SituationType defaultFor(String emotion) {
        return switch (emotion == null ? "" : emotion) {
            case "angry" -> APOLOGIZE;
            case "happy" -> CELEBRATE;
            case "surprise" -> EXPRESS;
            default -> COMFORT;
        };
    }

    public static String promptList() {
        return String.join(" | ", Arrays.stream(values()).map(SituationType::label).toList());
    }
}
