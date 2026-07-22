package com.hillingdon.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDistributionResponse {
    private long totalActive;
    private Category prebookedPatients;
    private Category driveInPatients;
    private Category doctors;
    private Category premiumParking;
    private double trendVsLastWeekPercent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private long count;
        private double percent;
    }
}
