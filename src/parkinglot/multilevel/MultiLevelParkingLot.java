package parkinglot.multilevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// MULTI-LEVEL PARKING GARAGE — the "extensibility" upgrade from the same article:
// https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/parking-lot
// ("How would you extend this to a multi-floor parking garage?")
//
// The article's answer: introduce a ParkingFloor entity between ParkingLot and
// ParkingSpot. Each floor owns its own spots, and finding a spot means iterating
// floor-by-floor (simplest strategy: fill lower floors first) instead of scanning
// one flat list. The Ticket also stores which floor it belongs to, so freeing a
// spot on exit doesn't require re-searching every floor.
//
// Kept fully independent from ParkingLot.java on purpose: separate enum/class
// names, no shared code, since this is meant to stand alone as the "upgraded"
// version rather than reuse the single-level classes.

enum VehicleCategory {
    MOTORCYCLE, CAR, LARGE
}

enum SpotCategory {
    MOTORCYCLE, CAR, LARGE
}

class GarageSpot {
    private final String id; // local to its floor, e.g. "C1"
    private final SpotCategory spotCategory;

    public GarageSpot(String id, SpotCategory spotCategory) {
        this.id = id;
        this.spotCategory = spotCategory;
    }

    public String getId() {
        return id;
    }

    public SpotCategory getSpotCategory() {
        return spotCategory;
    }
}

// One floor of the garage — owns its own spots and occupancy.
class ParkingFloor {
    private final int floorNumber;
    private final List<GarageSpot> spots;
    private final Set<String> occupiedSpotIds = new HashSet<>(); // local spot ids

    public ParkingFloor(int floorNumber, List<GarageSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getAvailableSpotCount(SpotCategory category) {
        int count = 0;
        for (GarageSpot spot : spots) {
            if (spot.getSpotCategory() == category && !occupiedSpotIds.contains(spot.getId())) {
                count++;
            }
        }
        return count;
    }

    public GarageSpot findAvailableSpot(SpotCategory category) {
        for (GarageSpot spot : spots) {
            if (spot.getSpotCategory() == category && !occupiedSpotIds.contains(spot.getId())) {
                return spot;
            }
        }
        return null;
    }

    public void markOccupied(String spotId) {
        occupiedSpotIds.add(spotId);
    }

    public void markFree(String spotId) {
        occupiedSpotIds.remove(spotId);
    }
}

// Immutable record of a parking session. Stores floorNumber explicitly (as the
// article suggests) so exit() doesn't need to re-search every floor to find
// which one owns this spot.
class GarageTicket {
    private final String id;
    private final int floorNumber;
    private final String spotId;
    private final VehicleCategory vehicleCategory;
    private final long entryTime;

    public GarageTicket(String id, int floorNumber, String spotId, VehicleCategory vehicleCategory, long entryTime) {
        this.id = id;
        this.floorNumber = floorNumber;
        this.spotId = spotId;
        this.vehicleCategory = vehicleCategory;
        this.entryTime = entryTime;
    }

    public String getId() {
        return id;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public String getSpotId() {
        return spotId;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public long getEntryTime() {
        return entryTime;
    }
}

public class MultiLevelParkingLot {

    private final List<ParkingFloor> floors;
    private final Map<String, GarageTicket> activeTickets;
    private final long hourlyRateCents;

    public MultiLevelParkingLot(List<ParkingFloor> floors, long hourlyRateCents) {
        this.floors = floors;
        this.activeTickets = new HashMap<>();
        this.hourlyRateCents = hourlyRateCents;
    }

    // Entry flow: fill lower floors first (the article's "simple floor iteration"
    // strategy) — check floor 1, then floor 2, and so on, until a spot is found.
    public GarageTicket enter(VehicleCategory vehicleCategory) {
        SpotCategory requiredSpotCategory = mapVehicleCategoryToSpotCategory(vehicleCategory);

        for (ParkingFloor floor : floors) {
            GarageSpot spot = floor.findAvailableSpot(requiredSpotCategory);
            if (spot != null) {
                floor.markOccupied(spot.getId());

                String ticketId = UUID.randomUUID().toString();
                long entryTime = System.currentTimeMillis();
                GarageTicket ticket = new GarageTicket(ticketId, floor.getFloorNumber(), spot.getId(), vehicleCategory, entryTime);

                activeTickets.put(ticketId, ticket);
                return ticket;
            }
        }

        throw new RuntimeException("No available spots for vehicle type " + vehicleCategory);
    }

    // Exit flow: identical shape to the single-level version, except the ticket
    // already knows which floor to free the spot on — no searching needed.
    public long exit(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            throw new RuntimeException("Invalid ticket ID");
        }

        GarageTicket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found or already used");
        }

        long exitTime = System.currentTimeMillis();
        long fee = computeFee(ticket.getEntryTime(), exitTime);

        ParkingFloor floor = floors.get(ticket.getFloorNumber() - 1);
        floor.markFree(ticket.getSpotId());
        activeTickets.remove(ticketId); // prevents double exit with the same ticket

        return fee;
    }

    private SpotCategory mapVehicleCategoryToSpotCategory(VehicleCategory vehicleCategory) {
        if (vehicleCategory == VehicleCategory.MOTORCYCLE) return SpotCategory.MOTORCYCLE;
        if (vehicleCategory == VehicleCategory.CAR) return SpotCategory.CAR;
        if (vehicleCategory == VehicleCategory.LARGE) return SpotCategory.LARGE;
        throw new RuntimeException("Unknown vehicle type");
    }

    private long computeFee(long entryTime, long exitTime) {
        long durationMillis = exitTime - entryTime;
        long durationHours = durationMillis / (1000 * 60 * 60);

        if (durationMillis % (1000 * 60 * 60) > 0) {
            durationHours++;
        }

        return durationHours * hourlyRateCents;
    }

    public static void main(String[] args) throws InterruptedException {
        // 2 floors, each with a single CAR spot, so we can watch overflow to floor 2.
        List<GarageSpot> floor1Spots = List.of(new GarageSpot("C1", SpotCategory.CAR));
        List<GarageSpot> floor2Spots = List.of(new GarageSpot("C1", SpotCategory.CAR));
        List<ParkingFloor> floors = new ArrayList<>();
        floors.add(new ParkingFloor(1, floor1Spots));
        floors.add(new ParkingFloor(2, floor2Spots));

        MultiLevelParkingLot garage = new MultiLevelParkingLot(floors, 500); // $5.00/hour

        GarageTicket ticket1 = garage.enter(VehicleCategory.CAR); // fills floor 1
        System.out.println("Entered: ticket=" + ticket1.getId() + " floor=" + ticket1.getFloorNumber());

        GarageTicket ticket2 = garage.enter(VehicleCategory.CAR); // floor 1 full -> overflow to floor 2
        System.out.println("Entered: ticket=" + ticket2.getId() + " floor=" + ticket2.getFloorNumber());

        try {
            garage.enter(VehicleCategory.CAR); // both floors full -> rejected
        } catch (RuntimeException e) {
            System.out.println("Rejected entry: " + e.getMessage());
        }

        Thread.sleep(1200);

        long fee = garage.exit(ticket2.getId()); // frees floor 2's spot
        System.out.println("Exited: fee=" + fee + " cents");

        GarageTicket ticket3 = garage.enter(VehicleCategory.CAR); // succeeds, goes into freed floor 2 spot
        System.out.println("Entered: ticket=" + ticket3.getId() + " floor=" + ticket3.getFloorNumber());
    }
}
