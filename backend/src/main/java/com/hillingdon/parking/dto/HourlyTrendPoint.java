package com.hillingdon.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyTrendPoint {
    private String hourLabel;
    private double occupancyPercent;
    private BigDecimal revenue;
}
