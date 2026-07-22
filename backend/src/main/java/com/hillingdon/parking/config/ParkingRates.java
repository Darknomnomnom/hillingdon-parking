package com.hillingdon.parking.config;

import com.hillingdon.parking.models.Bay;

import java.math.BigDecimal;
import java.util.Map;

/**
 * POC-only hourly rate constants used to estimate revenue KPIs, since no
 * pricing/billing table exists in the schema yet. Replace with a real
 * rates table post-approval.
 */
public final class ParkingRates {

    public static final Map<Bay.BayType, BigDecimal> HOURLY_RATES = Map.of(
            Bay.BayType.STANDARD, new BigDecimal("3.00"),
            Bay.BayType.ACCESSIBLE, new BigDecimal("3.00"),
            Bay.BayType.EV, new BigDecimal("2.00"),
            Bay.BayType.PREMIUM, new BigDecimal("5.00"),
            Bay.BayType.SPECIFIC_NEEDS, new BigDecimal("3.00")
    );

    // Flat walk-up fee assumed for drive-in parking, used only for the cost-savings KPI comparison
    public static final BigDecimal DRIVE_IN_SURCHARGE = new BigDecimal("1.00");

    private ParkingRates() {
    }
}
