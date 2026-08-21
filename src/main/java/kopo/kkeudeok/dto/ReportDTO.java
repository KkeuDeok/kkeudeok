package kopo.kkeudeok.dto;

import lombok.Builder;

import java.util.List;

// 성장 리포트
@Builder
public record ReportDTO(Metric understand,
                        Metric express,
                        Metric social,
                        // 감정별 이해율
                        List<EmotionRate> emotions,
                        // 감정별 표현 횟수
                        List<EmotionRate> expressEmotions,
                        // 표현 방법 비율
                        List<MethodShare> methodsNow,
                        List<MethodShare> methodsPrev,
                        // 체크리스트 4영역 지수
                        List<DomainIndex> domains,
                        // 로드맵 기준 몇 주차
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
