package kopo.kkeudeok.dto;

import java.util.Arrays;
import java.util.Optional;

// 함께할 친구 캐릭터
public enum CharacterType {

    TORI("tori", "토리", "마음을 함께 읽어주는 다정한 친구예요"),
    KOKO("koko", "코코", "궁금한 게 많은 씩씩한 친구예요"),
    LALA("lala", "라라", "노래하며 기분을 밝게 해 주는 친구예요"),
    BOMI("bomi", "보미", "언제나 웃으며 응원해 주는 친구예요"),
    BADA("bada", "바다", "천천히 기다려 주는 차분한 친구예요"),
    RUBI("rubi", "루비", "속상한 날 곁에 있어 주는 친구예요");

    public static final CharacterType DEFAULT = TORI;

    private final String key;
    private final String label;
    private final String description;

    CharacterType(String key, String label, String description) {
        this.key = key;
        this.label = label;
        this.description = description;
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public static Optional<CharacterType> of(String value) {

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String v = value.trim();

        return Arrays.stream(values())
                .filter(c -> c.key.equalsIgnoreCase(v)
                        || c.label.equals(v)
                        || c.name().equalsIgnoreCase(v))
                .findFirst();
    }

    public static CharacterType orDefault(String value) {
        return of(value).orElse(DEFAULT);
    }

    public static String labelOf(String value) {
        return orDefault(value).label();
    }
}
