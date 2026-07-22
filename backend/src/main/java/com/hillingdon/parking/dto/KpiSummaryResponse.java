package com.hillingdon.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiSummaryResponse {

    private Occupancy occupancy;
    private Occupancy specificNeeds;
    private Occupancy premiumParking;
    private Occupancy evCharging;

    private BigDecimal premiumRevenueToday;
    private RevenueBreakdown totalRevenueToday;

    private double avgParkingDurationMinutes;
    private Map<String, Double> avgDurationByZoneMinutes;

    private CostSavings parkingCostSavings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Occupancy {
        private long occupied;
        private long total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueBreakdown {
        private BigDecimal prebooked;
        private BigDecimal driveIn;
        private BigDecimal premium;
        private BigDecimal total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostSavings {
        private BigDecimal prebookedAvgCost;
        private BigDecimal driveInAvgCost;
        private String savingsMessage;
    }
}
