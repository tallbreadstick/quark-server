package com.darauy.quark.dto.response;

import com.darauy.quark.dto.request.ActivityRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ActivityContentResponse {
    private Integer id;
    private Integer idx;
    private String name;
    private String description;
    private String icon;
    private Ruleset ruleset;
    private String finishMessage;
    private List<Section> sections;

    @Data
    public static class Section {
        private Integer id;
        private Integer idx;
    }

    @Data
    public static class Ruleset {
        private Boolean enabled;
        private ActivityRequest.Ruleset.TimeExceededPenalty timeExceededPenalty;
        private ActivityRequest.Ruleset.DeductionStrategy deductionStrategy;
        private Float pointsDeduction;
        private LocalDateTime closeDateTime;
        private Long timeLimit;

        public enum TimeExceededPenalty {
            NO_TIME_LIMIT,
            CLOSE_ACTIVITY,
            DEDUCT_SCORE
        }

        public enum DeductionStrategy {
            FLAT,
            PERCENTAGE
        }
    }
}
