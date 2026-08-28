package kopo.kkeudeok.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ReportDTO(Metric understand,
                        Metric express,
                        Metric social,
                        List<EmotionRate> emotions,
                        List<EmotionRate> expressEmotions,
                        List<MethodShare> methodsNow,
                        List<MethodShare> methodsPrev,
                        List<DomainIndex> domains,
                        int weekNo,
                        boolean hasData) {

    @Builder
    public record Metric(int score, int delta, int total, int success, String basis) {
    }

    @Builder
    public record EmotionRate(String key, String label, int rate, int total) {
    }

    @Builder
    public record DomainIndex(String label, int score) {
    }

    @Builder
    public record MethodShare(String key, String label, int percent, int count) {
    }
}
