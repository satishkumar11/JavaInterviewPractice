package parkinglot.multilevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The article's answer to "how would you extend this to a multi-floor garage?":
// introduce ONE new class, ParkingFloor, between ParkingLot and ParkingSpot.
// Everything else (ParkingSpot, Ticket, VehicleType, SpotType) is unchanged from
// the single-level version — just redeclared here since Java packages can't
// share package-private classes across packages.
// https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/parking-lot

enum VehicleType {
    MOTORCYCLE, CAR, LARGE
}

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
    private final String spotId; // includes the floor implicitly, e.g. "2-C1"
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

// The one new entity the extension adds. Owns its own spots and occupancy.
class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;
    private final Set<String> occupiedSpotIds = new HashSet<>();

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getAvailableSpotCount(SpotType spotType) {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.getSpotType() == spotType && !occupiedSpotIds.contains(spot.getId())) {
                count++;
            }
        }
        return count;
    }

    public ParkingSpot findAvailableSpot(SpotType spotType) {
        for (ParkingSpot spot : spots) {
            if (spot.getSpotType() == spotType && !occupiedSpotIds.contains(spot.getId())) {
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

public class MultiLevelParkingLot {

    private final List<ParkingFloor> floors;
    private final Map<String, Ticket> activeTickets;
    private final long hourlyRateCents;

    public MultiLevelParkingLot(List<ParkingFloor> floors, long hourlyRateCents) {
        this.floors = floors;
        this.activeTickets = new HashMap<>();
        this.hourlyRateCents = hourlyRateCents;
    }

    // Simple floor iteration, straight from the article: check floor 1, then
    // floor 2, and so on, until a spot is found.
    public Ticket enter(VehicleType vehicleType) {
        SpotType requiredSpotType = mapVehicleTypeToSpotType(vehicleType);

        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(requiredSpotType);
            if (spot != null) {
                floor.markOccupied(spot.getId());

                String ticketId = UUID.randomUUID().toString();
                long entryTime = System.currentTimeMillis();
                Ticket ticket = new Ticket(ticketId, spot.getId(), vehicleType, entryTime);

                activeTickets.put(ticketId, ticket);
                return ticket;
            }
        }

        throw new RuntimeException("No available spots for vehicle type " + vehicleType);
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
        long fee = computeFee(ticket.getEntryTime(), exitTime);

        // Spot id is "<floorNumber>-<localId>", so the floor is recovered from it.
        int floorNumber = Integer.parseInt(ticket.getSpotId().split("-")[0]);
        floors.get(floorNumber - 1).markFree(ticket.getSpotId());
        activeTickets.remove(ticketId);

        return fee;
    }

    private SpotType mapVehicleTypeToSpotType(VehicleType vehicleType) {
        if (vehicleType == VehicleType.MOTORCYCLE) return SpotType.MOTORCYCLE;
        if (vehicleType == VehicleType.CAR) return SpotType.CAR;
        if (vehicleType == VehicleType.LARGE) return SpotType.LARGE;
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
        List<ParkingSpot> floor1Spots = List.of(new ParkingSpot("1-C1", SpotType.CAR));
        List<ParkingSpot> floor2Spots = List.of(new ParkingSpot("2-C1", SpotType.CAR));
        List<ParkingFloor> floors = new ArrayList<>();
        floors.add(new ParkingFloor(1, floor1Spots));
        floors.add(new ParkingFloor(2, floor2Spots));

        MultiLevelParkingLot garage = new MultiLevelParkingLot(floors, 500); // $5.00/hour

        Ticket ticket1 = garage.enter(VehicleType.CAR); // fills floor 1
        System.out.println("Entered: ticket=" + ticket1.getId() + " spot=" + ticket1.getSpotId());

        Ticket ticket2 = garage.enter(VehicleType.CAR); // floor 1 full -> overflow to floor 2
        System.out.println("Entered: ticket=" + ticket2.getId() + " spot=" + ticket2.getSpotId());

        try {
            garage.enter(VehicleType.CAR); // both floors full -> rejected
        } catch (RuntimeException e) {
            System.out.println("Rejected entry: " + e.getMessage());
        }

        Thread.sleep(1200);

        long fee = garage.exit(ticket2.getId()); // frees floor 2's spot
        System.out.println("Exited: fee=" + fee + " cents");

        Ticket ticket3 = garage.enter(VehicleType.CAR); // succeeds, goes into freed floor 2 spot
        System.out.println("Entered: ticket=" + ticket3.getId() + " spot=" + ticket3.getSpotId());
    }
}
