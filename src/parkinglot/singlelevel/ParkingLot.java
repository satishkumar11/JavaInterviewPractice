package parkinglot.singlelevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// SINGLE-LEVEL PARKING LOT — one flat list of spots, no floors.
// Faithful implementation of: https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/parking-lot
//
// Key design choices from the reference:
// - Vehicle is NOT a class, just an enum (it's external to the system, we only
//   care about its type).
// - ParkingSpot and Ticket are pure data holders (no occupancy logic on the spot).
// - Occupancy is relational state owned by ParkingLot, not the spot itself,
//   since "occupied" really means "referenced by an active ticket".
// - Fees are stored as long cents, never floating point, to avoid rounding errors.

// Not a class: Vehicle is external to our system, we don't manage or track it.
// We only need its type to match it with a compatible spot, so a single
// classification value (enum) is enough — no need for a full class.
enum VehicleType {
    MOTORCYCLE, CAR, LARGE
}

// SpotType and VehicleType have identical values but are kept as separate enums
// on purpose. A spot type is a different concept from a vehicle type, even if
// the labels match today. If a future rule appears (e.g. "motorcycles can use
// car spots when motorcycle spots are full"), having them separate keeps that
// logic clear instead of overloading one enum for two meanings.
enum SpotType {
    MOTORCYCLE, CAR, LARGE
}

// Deliberately simple: a pure data holder for the physical properties of a
// spot. It doesn't know about vehicles, tickets, pricing, or even whether it's
// occupied — occupancy is relational state, tracked by ParkingLot instead (see
// occupiedSpotIds below), since "occupied" really just means "referenced by an
// active ticket", not a property of the spot itself.
class ParkingSpot {
    private final String id;
    private final SpotType spotType;

    public ParkingSpot(String id, SpotType spotType) { this.id = id; this.spotType = spotType; }

    public String getId() { return id; }
    public SpotType getSpotType() { return spotType; }
}

// Immutable record of a parking session — created at entry, read-only after
// that.
class Ticket {
    private final String id;

    // Stored as a String ID, not a ParkingSpot reference. A Ticket is a record,
    // not a navigational object — it shouldn't be able to reach into the domain
    // model and call methods on a spot. Storing just the ID keeps tickets simple
    // and prevents that kind of accidental coupling. This is the Law of Demeter
    // in action: "talk to your immediate neighbors, don't reach through them."
    private final String spotId;

    private final VehicleType vehicleType;

    // Stored as a plain millisecond timestamp (rather than, say, a Date object)
    // because all we need is arithmetic: exitTime - entryTime to work out the
    // duration for billing.
    private final long entryTime;

    public Ticket(String id, String spotId, VehicleType vehicleType, long entryTime) {
        this.id = id;
        this.spotId = spotId;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
    }

    public String getId() { return id; }
    public String getSpotId() { return spotId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public long getEntryTime() { return entryTime; }
}

// Note: no getAvailableSpots() or getParkingStatus() method here. It might feel
// natural to expose the lot's internal state, but nothing in the requirements
// asks for it — adding it would just break encapsulation for no benefit. If a
// dashboard/monitoring need shows up later, add it then.
public class ParkingLot {

    private final List<ParkingSpot> spots;

    // Map instead of List: exit() needs to look up a ticket by its ID, and a Map
    // makes that "lookup by ID" intent explicit. In practice the performance
    // difference is negligible at parking-lot scale — a linear scan over a
    // realistic number of tickets is still microseconds — but the Map reads
    // more clearly.
    private final Map<String, Ticket> activeTickets;

    // Occupancy is tracked here, not on ParkingSpot itself. Ask "is this a
    // property of the entity, or a relationship the system manages?" — a spot
    // becomes occupied the moment we assign a ticket to it (before the car has
    // even reached it), so it's relational state, owned by whoever manages
    // tickets. That's ParkingLot, not the spot.
    private final Set<String> occupiedSpotIds;

    // Stored as long cents, never a float/double. Floats can't represent
    // decimal fractions exactly (0.1 has no exact binary representation), so
    // money math on them slowly accumulates rounding errors. Storing the
    // smallest unit (cents) as an integer keeps every calculation exact.
    private final long hourlyRateCents;

    public ParkingLot(List<ParkingSpot> spots, long hourlyRateCents) {
        this.spots = spots;
        this.activeTickets = new HashMap<>();
        this.occupiedSpotIds = new HashSet<>();
        this.hourlyRateCents = hourlyRateCents;
    }

    // Entry flow: find a free, type-matching spot and issue a ticket.
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

    // Exit flow: validate the ticket, calculate the fee, free the spot.
    public long exit(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            throw new RuntimeException("Invalid ticket ID");
        }

        // We don't distinguish "ticket never existed" from "ticket already used" —
        // both hit this same branch. A separate "used tickets" set could tell
        // them apart, but for this scope, treating both as one generic error is
        // simpler and good enough.
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found or already used");
        }

        long exitTime = System.currentTimeMillis();
        long fee = computeFee(ticket.getEntryTime(), exitTime);

        occupiedSpotIds.remove(ticket.getSpotId());
        activeTickets.remove(ticketId); // prevents double exit with the same ticket

        return fee;
    }

    // A plain first-match linear scan — no "prefer spots near the entrance" or
    // other smart allocation logic. Nothing in the requirements asked for that,
    // so adding it would be solving a problem nobody has yet. If the interviewer
    // wants smarter allocation, that's a follow-up question, not a base
    // requirement.
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
        if (vehicleType == VehicleType.MOTORCYCLE)
            return SpotType.MOTORCYCLE;
        if (vehicleType == VehicleType.CAR)
            return SpotType.CAR;
        if (vehicleType == VehicleType.LARGE)
            return SpotType.LARGE;
        throw new RuntimeException("Unknown vehicle type");
    }

    // Rounds any partial hour up to a full hour, e.g. 5 minutes still bills 1 hour.
    private long computeFee(long entryTime, long exitTime) {
        long durationMillis = exitTime - entryTime;
        long durationHours = durationMillis / (1000 * 60 * 60);

        if (durationMillis % (1000 * 60 * 60) > 0) {
            durationHours++;
        }

        return durationHours * hourlyRateCents;
    }

    public static void main(String[] args) throws InterruptedException {
        List<ParkingSpot> spots = List.of(
                new ParkingSpot("A", SpotType.MOTORCYCLE),
                new ParkingSpot("B", SpotType.CAR),
                new ParkingSpot("C", SpotType.LARGE));
        ParkingLot lot = new ParkingLot(spots, 500); // $5.00/hour

        Ticket ticket1 = lot.enter(VehicleType.CAR);
        System.out.println("Entered: ticket=" + ticket1.getId() + " spot=" + ticket1.getSpotId());

        try {
            lot.enter(VehicleType.CAR); // rejected, spot B already taken
        } catch (RuntimeException e) {
            System.out.println("Rejected entry: " + e.getMessage());
        }

        Thread.sleep(1200); // let some real time pass, well under an hour but still rounds up

        long fee = lot.exit(ticket1.getId());
        System.out.println("Exited: fee=" + fee + " cents");

        try {
            lot.exit(ticket1.getId()); // rejected, ticket already used
        } catch (RuntimeException e) {
            System.out.println("Rejected exit: " + e.getMessage());
        }

        Ticket ticket2 = lot.enter(VehicleType.CAR); // succeeds now, spot B was freed
        System.out.println("Entered again: ticket=" + ticket2.getId() + " spot=" + ticket2.getSpotId());
    }
}
