package com.darauy.quark.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class ChapterContentResponse {
    private Integer id;
    private Integer idx;
    private String name;
    private String description;
    private String icon;
    private List<ChapterItem> items;

    public enum ItemType {
        ACTIVITY,
        LESSON
    }

    @Data
    @Getter
    @Setter
    public static class ChapterItem {
        private Integer id;
        private ItemType itemType;
        private Integer idx;
        private String description;
        private String icon;
        private String finishMessage;
    }

    @Data
    public static class Activity extends ChapterItem {
        private String ruleset;

        @Builder(builderMethodName = "activityBuilder")
        public Activity(Integer id, Integer idx, String description, String icon, String finishMessage, String ruleset) {
            this.setId(id);
            this.setIdx(idx);
            this.setDescription(description);
            this.setIcon(icon);
            this.setFinishMessage(finishMessage);
            this.setItemType(ItemType.ACTIVITY);
            this.setRuleset(ruleset);
        }
    }

    @Data
    public static class Lesson extends ChapterItem {

        @Builder(builderMethodName = "lessonBuilder")
        public Lesson(Integer id, Integer idx, String description, String icon, String finishMessage) {
            this.setId(id);
            this.setIdx(idx);
            this.setDescription(description);
            this.setIcon(icon);
            this.setFinishMessage(finishMessage);
            this.setItemType(ItemType.LESSON);
        }
    }
}
