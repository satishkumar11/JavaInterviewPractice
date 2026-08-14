package parkinglot.pricing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Same structure as parkinglot.singlelevel.ParkingLot, but pricing is a
// PricingStrategy instead of a fixed rate. VehicleType, PricingStrategy, and
// the concrete strategies are reused from DynamicPricingDemo.java (same
// package, so no import needed).

enum SpotType {
    MOTORCYCLE, CAR, LARGE
}

class ParkingSpot {
    private final String id;
    private final SpotType spotType;

    public ParkingSpot(String id, SpotType spotType) {
        this.id = id;
        this.spotType = spotType;
    }

    public String getId() {
        return id;
    }

    public SpotType getSpotType() {
        return spotType;
    }
}

class Ticket {
    private final String id;
    private final String spotId;
    private final VehicleType vehicleType;
    private final long entryTime;

    public Ticket(String id, String spotId, VehicleType vehicleType, long entryTime) {
        this.id = id;
        this.spotId = spotId;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
    }

    public String getId() {
        return id;
    }

    public String getSpotId() {
        return spotId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public long getEntryTime() {
        return entryTime;
    }
}

public class DynamicPricingParkingLot {

    private final List<ParkingSpot> spots;
    private final Map<String, Ticket> activeTickets;
    private final Set<String> occupiedSpotIds;
    private PricingStrategy pricingStrategy;

    public DynamicPricingParkingLot(List<ParkingSpot> spots, PricingStrategy pricingStrategy) {
        this.spots = spots;
        this.activeTickets = new HashMap<>();
        this.occupiedSpotIds = new HashSet<>();
        this.pricingStrategy = pricingStrategy;
    }

    // Swap the active pricing rule at runtime
    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Ticket enter(VehicleType vehicleType) {
        ParkingSpot spot = findAvailableSpot(vehicleType);
        if (spot == null) {
            throw new RuntimeException("No available spots for vehicle type " + vehicleType);
        }

        occupiedSpotIds.add(spot.getId());

        String ticketId = UUID.randomUUID().toString();
        long entryTime = System.currentTimeMillis();
        Ticket ticket = new Ticket(ticketId, spot.getId(), vehicleType, entryTime);

        activeTickets.put(ticketId, ticket);
        return ticket;
    }

    public long exit(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            throw new RuntimeException("Invalid ticket ID");
        }

        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found or already used");
        }

        long exitTime = System.currentTimeMillis();
        long durationMillis = exitTime - ticket.getEntryTime();
        long fee = pricingStrategy.calculateFee(ticket.getVehicleType(), durationMillis);

        occupiedSpotIds.remove(ticket.getSpotId());
        activeTickets.remove(ticketId);

        return fee;
    }

    private ParkingSpot findAvailableSpot(VehicleType vehicleType) {
        SpotType requiredSpotType = mapVehicleTypeToSpotType(vehicleType);

        for (ParkingSpot spot : spots) {
            if (!occupiedSpotIds.contains(spot.getId()) && spot.getSpotType() == requiredSpotType) {
                return spot;
            }
        }
        return null;
    }

    private SpotType mapVehicleTypeToSpotType(VehicleType vehicleType) {
        if (vehicleType == VehicleType.MOTORCYCLE) return SpotType.MOTORCYCLE;
        if (vehicleType == VehicleType.CAR) return SpotType.CAR;
        if (vehicleType == VehicleType.LARGE) return SpotType.LARGE;
        throw new RuntimeException("Unknown vehicle type");
    }

    public static void main(String[] args) throws InterruptedException {
        List<ParkingSpot> spots = List.of(
                new ParkingSpot("A", SpotType.MOTORCYCLE),
                new ParkingSpot("B", SpotType.CAR),
                new ParkingSpot("C", SpotType.CAR),
                new ParkingSpot("D", SpotType.LARGE)
        );

        DynamicPricingParkingLot lot = new DynamicPricingParkingLot(spots, new HourlyPricing(500));

        Ticket ticket1 = lot.enter(VehicleType.CAR);
        Thread.sleep(1200);
        long fee1 = lot.exit(ticket1.getId());
        System.out.println("Flat rate -> CAR fee: " + fee1 + " cents");

        Map<VehicleType, Long> rates = new HashMap<>();
        rates.put(VehicleType.MOTORCYCLE, 300L);
        rates.put(VehicleType.CAR, 500L);
        rates.put(VehicleType.LARGE, 800L);
        lot.setPricingStrategy(new VehicleTypePricing(rates));

        Ticket ticket2 = lot.enter(VehicleType.LARGE);
        Thread.sleep(1200);
        long fee2 = lot.exit(ticket2.getId());
        System.out.println("Per-type -> LARGE fee: " + fee2 + " cents");

        lot.setPricingStrategy(new SurgePricing(new VehicleTypePricing(rates), 1.5));

        Ticket ticket3 = lot.enter(VehicleType.CAR);
        Thread.sleep(1200);
        long fee3 = lot.exit(ticket3.getId());
        System.out.println("Surge (1.5x) -> CAR fee: " + fee3 + " cents");
    }
}
