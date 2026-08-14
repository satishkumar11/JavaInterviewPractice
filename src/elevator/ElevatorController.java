package elevator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// ELEVATOR SYSTEM — 3 elevators serving 10 floors (0-9), simulated in discrete
// ticks via step(). Faithful implementation of the hellointerview.com
// elevator-system breakdown: hall calls (direction-aware) are dispatched by
// the controller; destination calls (no direction) are added directly on the
// elevator the rider is already inside. Each car uses the SCAN algorithm:
// sweep in one direction until nothing's left ahead, then reverse.

// Which way an elevator (or a hall call) is heading.
enum Direction {
    UP, DOWN, IDLE
}

// The kind of stop being requested.
enum RequestType {
    PICKUP_UP, PICKUP_DOWN, DESTINATION
}

// One stop an elevator needs to make — a floor plus why it's stopping there.
class Request {
    private final int floor;
    private final RequestType type;

    public Request(int floor, RequestType type) { this.floor = floor; this.type = type; }

    public int getFloor() { return floor; }
    public RequestType getType() { return type; }

    // Value equality (not identity) so the Set can dedupe and so a freshly
    // constructed Request can be used to look up a matching one already queued.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        Request other = (Request) o;
        return floor == other.floor && type == other.type;
    }

    @Override
    public int hashCode() { return Objects.hash(floor, type); }
}

// A single elevator car: tracks its own position, direction, and pending stops.
class Elevator {
    private int currentFloor;
    private Direction direction;
    private final Set<Request> requests;

    public Elevator() {
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.requests = new HashSet<>();
    }

    public boolean addRequest(Request request) {
        if (request.getFloor() < 0 || request.getFloor() > 9) return false;
        if (request.getFloor() == currentFloor) return true; // already here, no-op
        if (requests.contains(request)) return false;
        return requests.add(request);
    }

    public void step() {
        // Case 1: nothing queued - go idle.
        if (requests.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }

        // Case 2: idle with new requests - pick a direction toward the nearest one.
        // Deterministic tiebreak (lowest floor) since Set iteration order isn't.
        if (direction == Direction.IDLE) {
            Request nearest = null;
            int minDistance = Integer.MAX_VALUE;
            for (Request req : requests) {
                int distance = Math.abs(req.getFloor() - currentFloor);
                if (distance < minDistance || (distance == minDistance && (nearest == null || req.getFloor() < nearest.getFloor()))) {
                    minDistance = distance;
                    nearest = req;
                }
            }
            direction = (nearest.getFloor() > currentFloor) ? Direction.UP : Direction.DOWN;
        }

        // Case 3: stop here if there's a pickup going our direction or a destination.
        RequestType pickupType = (direction == Direction.UP) ? RequestType.PICKUP_UP : RequestType.PICKUP_DOWN;
        Request pickupRequest = new Request(currentFloor, pickupType);
        Request destinationRequest = new Request(currentFloor, RequestType.DESTINATION);

        if (requests.contains(pickupRequest) || requests.contains(destinationRequest)) {
            requests.remove(pickupRequest);
            requests.remove(destinationRequest);
            if (requests.isEmpty()) direction = Direction.IDLE;
            return; // stopped this tick, don't also move
        }

        // Case 4: nothing left ahead in this direction - reverse.
        if (!hasRequestsAhead(direction)) {
            direction = (direction == Direction.UP) ? Direction.DOWN : Direction.UP;
            return;
        }

        // Case 5: keep going.
        if (direction == Direction.UP) currentFloor++;
        else if (direction == Direction.DOWN) currentFloor--;
    }

    // Any request further along in this direction, regardless of type - the car
    // travels toward everything ahead even if it won't stop until it reverses.
    public boolean hasRequestsAhead(Direction dir) {
        for (Request request : requests) {
            if (dir == Direction.UP && request.getFloor() > currentFloor) return true;
            if (dir == Direction.DOWN && request.getFloor() < currentFloor) return true;
        }
        return false;
    }

    public boolean hasRequestsAtOrBeyond(int floor, Direction dir) {
        for (Request request : requests) {
            if (dir == Direction.UP && request.getFloor() >= floor) {
                if (request.getType() == RequestType.PICKUP_UP || request.getType() == RequestType.DESTINATION) return true;
            }
            if (dir == Direction.DOWN && request.getFloor() <= floor) {
                if (request.getType() == RequestType.PICKUP_DOWN || request.getType() == RequestType.DESTINATION) return true;
            }
        }
        return false;
    }

    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
}

// Orchestrator: dispatches hall calls to the best elevator; doesn't know how
// elevators move, just tells each one to advance a tick.
public class ElevatorController {
    private final List<Elevator> elevators;

    public ElevatorController() {
        elevators = new ArrayList<>();
        elevators.add(new Elevator());
        elevators.add(new Elevator());
        elevators.add(new Elevator());
    }

    public boolean requestElevator(int floor, RequestType type) {
        if (floor < 0 || floor > 9) return false;
        if (type == RequestType.DESTINATION) return false; // hall calls only

        Request request = new Request(floor, type);
        Elevator best = selectBestElevator(request);
        return best.addRequest(request);
    }

    // Simplest strategy: nearest car by raw distance. Doesn't account for
    // direction or whether the car will sail past the floor before it can
    // stop - the article flags this as the natural next improvement.
    private Elevator selectBestElevator(Request request) {
        Elevator best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getCurrentFloor() - request.getFloor());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = elevator;
            }
        }
        return best;
    }

    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step();
        }
    }

    // Not in the doc's method table, but needed for the demo below to observe
    // per-elevator state and to add destination requests directly on a car.
    public List<Elevator> getElevators() { return elevators; }

    public static void main(String[] args) {
        ElevatorController controller = new ElevatorController();
        List<Elevator> elevators = controller.getElevators();
        Elevator e0 = elevators.get(0);

        controller.requestElevator(5, RequestType.PICKUP_UP);
        System.out.println("Hall call floor 5 UP -> dispatched (all cars idle at floor 0, elevator 0 wins the tie)");

        while (e0.getCurrentFloor() != 5) {
            controller.step();
            System.out.println("e0 at " + e0.getCurrentFloor() + " (" + e0.getDirection() + ")");
        }
        System.out.println("e0 arrived at floor 5, doors open");

        e0.addRequest(new Request(9, RequestType.DESTINATION));
        System.out.println("Rider inside e0 selected floor 9");

        controller.requestElevator(3, RequestType.PICKUP_UP);
        System.out.println("Hall call floor 3 UP -> naive nearest-distance dispatch picks e0 (already at 5, dist 2)"
                + " over the idle cars sitting at floor 0 (dist 3) - even though e0 is heading UP, away from floor 3");

        // direction only returns to IDLE once every queued request (9-DEST, then
        // 3-PICKUP_UP) has actually been served, so this is a safe stop condition.
        while (e0.getDirection() != Direction.IDLE) {
            controller.step();
            System.out.println("e0 at " + e0.getCurrentFloor() + " (" + e0.getDirection() + ")");
        }
        System.out.println("e0 finally serves floor 3 after detouring to 9 and back - the dispatch blind spot the article warns about");
    }
}
