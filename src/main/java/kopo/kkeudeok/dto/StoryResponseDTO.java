package kopo.kkeudeok.dto;

import lombok.Builder;

import java.util.List;
public final class StoryResponseDTO {

    private StoryResponseDTO() {}

    @Builder
    public record Start(Long sessionId, Long storyId, Integer storySeq, int dailyGoal, String emotion, String title,
                        String source, String childCallName, String characterKey, String gesture,
                        StoryNodeDTO node) { }

    @Builder
    public record Next(
            StoryNodeDTO node,

            boolean last
    ) {
    }

    @Builder
    public record Finish(Long sessionId, String status, int savedCount, int todayDone, int dailyGoal) { }

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
            String resumeScreen,
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
