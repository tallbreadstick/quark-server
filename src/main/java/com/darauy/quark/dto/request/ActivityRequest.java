package com.darauy.quark.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRequest {
    private String name;
    private String description;
    private String icon;
    private Ruleset ruleset;
    private String finishMessage;

    @Data
    public static class Ruleset {
        private Boolean enabled;
        private TimeExceededPenalty timeExceededPenalty;
        private DeductionStrategy deductionStrategy;
        private Float pointsDeduction;
        private LocalDate closeDate;
        private LocalTime closeTime;
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
