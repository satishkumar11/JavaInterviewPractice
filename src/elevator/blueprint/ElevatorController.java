package elevator.blueprint;

import java.util.List;
import java.util.Set;

// STRUCTURE ONLY — same classes, fields, and method signatures as
// elevator.ElevatorController, but every body is a stub. No business logic here.

// Which way an elevator (or a hall call) is heading.
enum Direction {
    UP, DOWN, IDLE
}

// The kind of stop being requested.
enum RequestType {
    PICKUP_UP, PICKUP_DOWN, DESTINATION
}

// One stop an elevator needs to make - a floor plus why it's stopping there.
class Request {
    private final int floor;
    private final RequestType type;

    public Request(int floor, RequestType type) { this.floor = 0; this.type = null; }

    public int getFloor() { return 0; }
    public RequestType getType() { return null; }

    @Override
    public boolean equals(Object o) { return false; }

    @Override
    public int hashCode() { return 0; }
}

// A single elevator car: tracks its own position, direction, and pending stops.
class Elevator {
    private int currentFloor;
    private Direction direction;
    private final Set<Request> requests;

    public Elevator() {
        this.currentFloor = 0;
        this.direction = null;
        this.requests = null;
    }

    public boolean addRequest(Request request) { return false; }
    public void step() { }
    public boolean hasRequestsAhead(Direction dir) { return false; }
    public boolean hasRequestsAtOrBeyond(int floor, Direction dir) { return false; }
    public int getCurrentFloor() { return 0; }
    public Direction getDirection() { return null; }
}

// Orchestrator: dispatches hall calls to the best elevator; doesn't know how
// elevators move, just tells each one to advance a tick.
public class ElevatorController {
    private final List<Elevator> elevators;

    public ElevatorController() { this.elevators = null; }

    public boolean requestElevator(int floor, RequestType type) { return false; }

    private Elevator selectBestElevator(Request request) { return null; }

    public void step() { }
    public List<Elevator> getElevators() { return null; }
}
