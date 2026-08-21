package kopo.kkeudeok.dto;

import lombok.Data;

import java.util.List;

public final class StoryRequestDTO {

    private StoryRequestDTO() {
    }

    @Data
    public static class Start {

        private Long childId;
        private String emotion;
        private String dailyInput;
        private boolean prepare;
    }

    @Data
    public static class Next {

        private String stageType;
        private String missionType;
        private String responseValue;
        private Boolean success;
    }

    @Data
    public static class Finish {

        private boolean completed;
        private List<Item> results;

        @Data
        public static class Item {

            private Integer nodeOrder;
            private String missionType;
            private String responseValue;
            private boolean success;
        }
    }
}
