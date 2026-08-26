package kopo.kkeudeok.dto;

import lombok.Builder;

import java.util.List;
public final class StoryResponseDTO {

    private StoryResponseDTO() {}

    @Builder
    public record Start(Long sessionId, Long storyId, Integer storySeq, int dailyGoal, String emotion, String title,
                        String source, String childCallName, String characterKey, String gesture,
                        StoryNodeDTO node) { }

    // 다음 노드 응답
    @Builder
    public record Next(
            StoryNodeDTO node,

            // 이 노드가 마지막(칭찬)인가
            boolean last
    ) {
    }

    // 세션 종료 응답
    @Builder
    public record Finish(Long sessionId, String status, int savedCount, int todayDone, int dailyGoal) { }

    // 이어하기 응답
    @Builder
    public record Resume(

            boolean found,
            Long sessionId,
            Long storyId,
            Integer storySeq,
            int dailyGoal,
            String title,
            String childCallName,
            String characterKey,
            String gesture,
            // 이어하기 마지막 화면
            String resumeScreen,
            // 이어하기 노드
            StoryNodeDTO node
    ) {
    }

    @Builder
    public record Fail(String code, String message) { }

    @Builder
    public record Summary(int streakDays, int todayDone, int dailyGoal, List<String> activeDays, List<Record> recent) {
        @Builder
        public record Record(int no, String date, String title, boolean completed) {
        }
    }
}
