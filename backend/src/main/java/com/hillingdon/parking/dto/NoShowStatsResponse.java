package com.hillingdon.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoShowStatsResponse {
    private long todayCount;
    private long weekCount;
    private List<Bucket> byTimeOfDay;
    private List<Bucket> byDayOfWeek;
    private String insight;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bucket {
        private String label;
        private long count;
    }
}
