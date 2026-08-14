package parkinglot.pricing;

import java.util.Map;

// DYNAMIC PRICING — swap out how the parking fee is calculated (flat rate,
// per-vehicle-type, surge) using the Strategy Pattern.
// https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/parking-lot

enum VehicleType {
    MOTORCYCLE, CAR, LARGE
}

// Strategy interface
interface PricingStrategy {
    long calculateFee(VehicleType vehicleType, long durationMillis);
}

// Same hourly rate for every vehicle type
class HourlyPricing implements PricingStrategy {
    private final long hourlyRateCents;

    public HourlyPricing(long hourlyRateCents) {
        this.hourlyRateCents = hourlyRateCents;
    }

    public long calculateFee(VehicleType vehicleType, long durationMillis) {
        return roundedUpHours(durationMillis) * hourlyRateCents;
    }

    static long roundedUpHours(long durationMillis) {
        long hourInMillis = 1000 * 60 * 60;
        long hours = durationMillis / hourInMillis;
        if (durationMillis % hourInMillis > 0) {
            hours++;
        }
        return hours;
    }
}

// Different hourly rate per vehicle type
class VehicleTypePricing implements PricingStrategy {
    private final Map<VehicleType, Long> hourlyRatesCents;

    public VehicleTypePricing(Map<VehicleType, Long> hourlyRatesCents) {
        this.hourlyRatesCents = hourlyRatesCents;
    }

    public long calculateFee(VehicleType vehicleType, long durationMillis) {
        long rate = hourlyRatesCents.get(vehicleType);
        return HourlyPricing.roundedUpHours(durationMillis) * rate;
    }
}

// Wraps another strategy and multiplies its fee (e.g. 1.5x during peak hours)
class SurgePricing implements PricingStrategy {
    private final PricingStrategy basePricingStrategy;
    private final double surgeMultiplier;

    public SurgePricing(PricingStrategy basePricingStrategy, double surgeMultiplier) {
        this.basePricingStrategy = basePricingStrategy;
        this.surgeMultiplier = surgeMultiplier;
    }

    public long calculateFee(VehicleType vehicleType, long durationMillis) {
        long baseFee = basePricingStrategy.calculateFee(vehicleType, durationMillis);
        return Math.round(baseFee * surgeMultiplier);
    }
}

// See DynamicPricingParkingLot.java for the "context" that uses these strategies.
