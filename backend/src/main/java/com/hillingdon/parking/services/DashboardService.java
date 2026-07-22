package com.hillingdon.parking.services;

import com.hillingdon.parking.config.ParkingRates;
import com.hillingdon.parking.dto.FloorBreakdownItem;
import com.hillingdon.parking.dto.HourlyTrendPoint;
import com.hillingdon.parking.dto.KpiSummaryResponse;
import com.hillingdon.parking.dto.NoShowStatsResponse;
import com.hillingdon.parking.dto.UserDistributionResponse;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.Floor;
import com.hillingdon.parking.repositories.BayRepository;
import com.hillingdon.parking.repositories.BookingRepository;
import com.hillingdon.parking.repositories.FloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NOTE: the ANPR simulator only fires ENTRY events for pre-booked plates and never
 * fires EXIT events or drive-in reads (see AnprService/ANPRSimulatorJob), so there is
 * no real "completed parking session" history to derive duration/revenue from. Revenue
 * and duration KPIs are instead computed from a live snapshot of currently ARRIVED
 * bookings (elapsed time since appointmentTime x POC rate), not historical sessions.
 * Drive-in and Doctors buckets are 0 until those data sources exist.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.of("Europe/London");

    private final BayRepository bayRepository;
    private final FloorRepository floorRepository;
    private final BookingRepository bookingRepository;

    public KpiSummaryResponse getKpiSummary() {
        KpiSummaryResponse response = new KpiSummaryResponse();

        response.setOccupancy(new KpiSummaryResponse.Occupancy(
                bayRepository.countByStatus(Bay.BayStatus.OCCUPIED), bayRepository.count()));
        response.setSpecificNeeds(new KpiSummaryResponse.Occupancy(
                bayRepository.countByTypeAndStatus(Bay.BayType.SPECIFIC_NEEDS, Bay.BayStatus.OCCUPIED),
                bayRepository.countByType(Bay.BayType.SPECIFIC_NEEDS)));
        response.setPremiumParking(new KpiSummaryResponse.Occupancy(
                bayRepository.countByTypeAndStatus(Bay.BayType.PREMIUM, Bay.BayStatus.OCCUPIED),
                bayRepository.countByType(Bay.BayType.PREMIUM)));
        response.setEvCharging(new KpiSummaryResponse.Occupancy(
                bayRepository.countByTypeAndStatus(Bay.BayType.EV, Bay.BayStatus.OCCUPIED),
                bayRepository.countByType(Bay.BayType.EV)));

        List<Booking> arrived = bookingRepository.findByStatus(Booking.BookingStatus.ARRIVED);
        Instant now = Instant.now();

        BigDecimal prebookedRevenue = BigDecimal.ZERO;
        BigDecimal premiumRevenue = BigDecimal.ZERO;
        long totalMinutes = 0;
        Map<String, List<Long>> minutesByZone = new LinkedHashMap<>();

        for (Booking b : arrived) {
            if (b.getBay() == null) continue;
            long minutes = Math.max(0, Duration.between(b.getAppointmentTime(), now).toMinutes());
            totalMinutes += minutes;

            Bay bay = b.getBay();
            BigDecimal rate = ParkingRates.HOURLY_RATES.get(bay.getType());
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            BigDecimal revenue = rate.multiply(hours).setScale(2, RoundingMode.HALF_UP);

            if (bay.isPremium()) {
                premiumRevenue = premiumRevenue.add(revenue);
            } else {
                prebookedRevenue = prebookedRevenue.add(revenue);
            }

            String zone = bay.getFloor() != null ? bay.getFloor().getName() : "Unknown";
            minutesByZone.computeIfAbsent(zone, z -> new ArrayList<>()).add(minutes);
        }

        BigDecimal driveInRevenue = BigDecimal.ZERO; // TODO: no drive-in ANPR simulation yet
        response.setPremiumRevenueToday(premiumRevenue);
        response.setTotalRevenueToday(new KpiSummaryResponse.RevenueBreakdown(
                prebookedRevenue, driveInRevenue, premiumRevenue,
                prebookedRevenue.add(driveInRevenue).add(premiumRevenue)));

        response.setAvgParkingDurationMinutes(arrived.isEmpty() ? 0 : (double) totalMinutes / arrived.size());

        Map<String, Double> avgByZone = minutesByZone.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().mapToLong(Long::longValue).average().orElse(0),
                        (a, b2) -> a, LinkedHashMap::new));
        response.setAvgDurationByZoneMinutes(avgByZone);

        BigDecimal avgHours = BigDecimal.valueOf(response.getAvgParkingDurationMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal avgStandardRate = ParkingRates.HOURLY_RATES.get(Bay.BayType.STANDARD);
        BigDecimal prebookedAvgCost = avgStandardRate.multiply(avgHours).setScale(2, RoundingMode.HALF_UP);
        BigDecimal driveInAvgCost = avgStandardRate.add(ParkingRates.DRIVE_IN_SURCHARGE)
                .multiply(avgHours).setScale(2, RoundingMode.HALF_UP);
        BigDecimal savings = driveInAvgCost.subtract(prebookedAvgCost);
        response.setParkingCostSavings(new KpiSummaryResponse.CostSavings(
                prebookedAvgCost, driveInAvgCost,
                "Pre-booking saves patients an estimated £" + savings + " per visit compared to drive-in parking."));

        return response;
    }

    public List<FloorBreakdownItem> getFloorBreakdown() {
        List<FloorBreakdownItem> items = new ArrayList<>();
        for (Floor floor : floorRepository.findAllByOrderByNumberAsc()) {
            long occupied = bayRepository.countByFloorAndStatus(floor, Bay.BayStatus.OCCUPIED);
            long total = floor.getTotalBays();
            long evTotal = bayRepository.countByFloorAndType(floor, Bay.BayType.EV);
            long evOccupied = bayRepository.countByFloorAndTypeAndStatus(floor, Bay.BayType.EV, Bay.BayStatus.OCCUPIED);
            double percentFull = total == 0 ? 0 : (occupied * 100.0) / total;

            items.add(new FloorBreakdownItem(floor.getNumber(), floor.getName(), occupied, total,
                    percentFull, evTotal, evOccupied, occupied));
        }
        return items;
    }

    public List<HourlyTrendPoint> getHourlyTrends() {
        List<HourlyTrendPoint> points = new ArrayList<>();
        List<Booking> arrived = bookingRepository.findByStatus(Booking.BookingStatus.ARRIVED);
        long totalBays = bayRepository.count();
        ZonedDateTime nowZoned = ZonedDateTime.now(ZONE);
        Instant now = nowZoned.toInstant();

        for (int i = 11; i >= 0; i--) {
            ZonedDateTime hourEnd = nowZoned.minusHours(i).withMinute(59).withSecond(59);
            Instant hourEndInstant = hourEnd.toInstant();
            ZonedDateTime hourStart = hourEnd.withMinute(0).withSecond(0);

            long cumulativeArrivals = arrived.stream()
                    .filter(b -> !b.getAppointmentTime().isAfter(hourEndInstant))
                    .count();
            double occupancyPercent = totalBays == 0 ? 0 : (cumulativeArrivals * 100.0) / totalBays;

            BigDecimal revenueThisHour = arrived.stream()
                    .filter(b -> b.getBay() != null
                            && !b.getAppointmentTime().isBefore(hourStart.toInstant())
                            && !b.getAppointmentTime().isAfter(hourEndInstant))
                    .map(b -> {
                        long minutes = Math.max(0, Duration.between(b.getAppointmentTime(), now).toMinutes());
                        BigDecimal rate = ParkingRates.HOURLY_RATES.get(b.getBay().getType());
                        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
                        return rate.multiply(hours);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            String label = hourEnd.getHour() + ":00";
            points.add(new HourlyTrendPoint(label, occupancyPercent, revenueThisHour));
        }
        return points;
    }

    public NoShowStatsResponse getNoShowStats() {
        ZonedDateTime todayStart = ZonedDateTime.now(ZONE).toLocalDate().atStartOfDay(ZONE);
        Instant weekAgo = Instant.now().minus(Duration.ofDays(7));
        Instant now = Instant.now();

        List<Booking> noShowsThisWeek = bookingRepository.findByStatusAndArrivalWindowStartBetween(
                Booking.BookingStatus.NO_SHOW, weekAgo, now);

        long todayCount = noShowsThisWeek.stream()
                .filter(b -> !b.getArrivalWindowStart().isBefore(todayStart.toInstant()))
                .count();

        Map<String, Long> byTimeOfDay = new LinkedHashMap<>();
        byTimeOfDay.put("Morning (06-12)", 0L);
        byTimeOfDay.put("Afternoon (12-17)", 0L);
        byTimeOfDay.put("Evening (17-21)", 0L);
        byTimeOfDay.put("Night (21-06)", 0L);

        Map<DayOfWeek, Long> byDay = new LinkedHashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) byDay.put(d, 0L);

        for (Booking b : noShowsThisWeek) {
            ZonedDateTime zdt = b.getArrivalWindowStart().atZone(ZONE);
            int hour = zdt.getHour();
            String bucket = hour >= 6 && hour < 12 ? "Morning (06-12)"
                    : hour >= 12 && hour < 17 ? "Afternoon (12-17)"
                    : hour >= 17 && hour < 21 ? "Evening (17-21)"
                    : "Night (21-06)";
            byTimeOfDay.merge(bucket, 1L, Long::sum);
            byDay.merge(zdt.getDayOfWeek(), 1L, Long::sum);
        }

        List<NoShowStatsResponse.Bucket> timeOfDayBuckets = byTimeOfDay.entrySet().stream()
                .map(e -> new NoShowStatsResponse.Bucket(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        List<NoShowStatsResponse.Bucket> dayOfWeekBuckets = byDay.entrySet().stream()
                .map(e -> new NoShowStatsResponse.Bucket(e.getKey().getDisplayName(TextStyle.SHORT, Locale.UK), e.getValue()))
                .collect(Collectors.toList());

        String insight = "Not enough data yet to identify a no-show pattern.";
        long maxTimeCount = timeOfDayBuckets.stream().mapToLong(NoShowStatsResponse.Bucket::getCount).max().orElse(0);
        long maxDayCount = dayOfWeekBuckets.stream().mapToLong(NoShowStatsResponse.Bucket::getCount).max().orElse(0);
        if (maxTimeCount > 0 && maxDayCount > 0) {
            String topTime = timeOfDayBuckets.stream().filter(b -> b.getCount() == maxTimeCount).findFirst().get().getLabel();
            String topDay = dayOfWeekBuckets.stream().filter(b -> b.getCount() == maxDayCount).findFirst().get().getLabel();
            insight = "Most no-shows occur on " + topDay + " during the " + topTime + " slot.";
        }

        return new NoShowStatsResponse(todayCount, noShowsThisWeek.size(), timeOfDayBuckets, dayOfWeekBuckets, insight);
    }

    public UserDistributionResponse getUserDistribution() {
        List<Booking> arrived = bookingRepository.findByStatus(Booking.BookingStatus.ARRIVED);

        long premiumCount = arrived.stream().filter(b -> b.getBay() != null && b.getBay().isPremium()).count();
        long prebookedCount = arrived.size() - premiumCount;
        long driveInCount = 0; // TODO: ANPR simulator doesn't simulate drive-ins yet
        long doctorsCount = 0; // TODO: needs a staff/doctor plate registry (see CLAUDE.md)

        long totalActive = prebookedCount + premiumCount + driveInCount + doctorsCount;

        Instant now = Instant.now();
        Instant weekAgo = now.minus(Duration.ofDays(7));
        Instant twoWeeksAgo = now.minus(Duration.ofDays(14));
        long createdThisWeek = bookingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt().isAfter(weekAgo)).count();
        long createdPriorWeek = bookingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt().isAfter(twoWeeksAgo) && !b.getCreatedAt().isAfter(weekAgo)).count();
        double trend = createdPriorWeek == 0 ? 0
                : ((createdThisWeek - createdPriorWeek) * 100.0) / createdPriorWeek;

        return new UserDistributionResponse(
                totalActive,
                toCategory(prebookedCount, totalActive),
                toCategory(driveInCount, totalActive),
                toCategory(doctorsCount, totalActive),
                toCategory(premiumCount, totalActive),
                trend);
    }

    private UserDistributionResponse.Category toCategory(long count, long total) {
        double percent = total == 0 ? 0 : (count * 100.0) / total;
        return new UserDistributionResponse.Category(count, percent);
    }
}
