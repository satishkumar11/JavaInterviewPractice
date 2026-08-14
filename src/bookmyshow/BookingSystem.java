package bookmyshow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

// A film that can be showing at multiple theaters and times.
class Movie {
    private final String id;
    private final String title;

    public Movie(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}

// A physical location; owns the showtimes playing there.
class Theater {
    private final String id;
    private final String name;
    private final List<Showtime> showtimes;

    public Theater(String id, String name) {
        this.id = id;
        this.name = name;
        this.showtimes = new ArrayList<>();
    }

    // Not in the doc's method table, but needed: showtimes are added after
    // construction since each Showtime needs a reference back to its Theater.
    public void addShowtime(Showtime showtime) {
        showtimes.add(showtime);
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public List<Showtime> getShowtimesForMovie(Movie movie) {
        List<Showtime> results = new ArrayList<>();
        for (Showtime showtime : showtimes) {
            if (showtime.getMovie().getId().equals(movie.getId())) {
                results.add(showtime);
            }
        }
        return results;
    }
}

// One bookable screening: tracks reservations and derives seat availability from them.
class Showtime {
    private static final int MIN_SEAT_NUM = 0;
    private static final int MAX_SEAT_NUM = 20; // rows A-Z, seats 0-20 (546 seats)

    private final String id;
    private final Theater theater;
    private final Movie movie;
    private final LocalDateTime datetime;
    private final String screenLabel;
    private final List<Reservation> reservations;

    public Showtime(String id, Theater theater, Movie movie, LocalDateTime datetime, String screenLabel) {
        this.id = id;
        this.theater = theater;
        this.movie = movie;
        this.datetime = datetime;
        this.screenLabel = screenLabel;
        this.reservations = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Theater getTheater() {
        return theater;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public Movie getMovie() {
        return movie;
    }

    public boolean isAvailable(String seatId) {
        for (Reservation reservation : reservations) {
            if (reservation.getSeatIds().contains(seatId)) {
                return false;
            }
        }
        return true;
    }

    public List<String> getAvailableSeats() {
        Set<String> booked = new HashSet<>();
        for (Reservation reservation : reservations) {
            booked.addAll(reservation.getSeatIds());
        }

        List<String> available = new ArrayList<>();
        for (char row = 'A'; row <= 'Z'; row++) {
            for (int num = MIN_SEAT_NUM; num <= MAX_SEAT_NUM; num++) {
                String seatId = row + String.valueOf(num);
                if (!booked.contains(seatId)) {
                    available.add(seatId);
                }
            }
        }
        return available;
    }

    // synchronized: check-and-store must be atomic so exactly one concurrent
    // booking of the same seat succeeds (requirement R6).
    public synchronized void book(Reservation reservation) {
        List<String> seatIds = reservation.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Must select at least one seat");
        }

        for (String seatId : seatIds) {
            if (!isValidSeatId(seatId)) {
                throw new IllegalArgumentException("Invalid seat: " + seatId);
            }
        }

        for (String seatId : seatIds) {
            if (!isAvailable(seatId)) {
                throw new IllegalStateException("Seat unavailable: " + seatId);
            }
        }

        reservations.add(reservation);
    }

    public synchronized void cancel(Reservation reservation) {
        reservations.remove(reservation);
    }

    private boolean isValidSeatId(String seatId) {
        if (seatId == null || seatId.length() < 2) {
            return false;
        }
        char row = seatId.charAt(0);
        if (row < 'A' || row > 'Z') {
            return false;
        }
        try {
            int num = Integer.parseInt(seatId.substring(1));
            return num >= MIN_SEAT_NUM && num <= MAX_SEAT_NUM;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

// A user's booking record; used to route cancellations back to the right showtime.
class Reservation {
    private final String confirmationId;
    private final Showtime showtime;
    private final List<String> seatIds;

    public Reservation(String confirmationId, Showtime showtime, List<String> seatIds) {
        this.confirmationId = confirmationId;
        this.showtime = showtime;
        this.seatIds = new ArrayList<>(seatIds); // defensive copy
    }

    public String getConfirmationId() {
        return confirmationId;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public List<String> getSeatIds() {
        return new ArrayList<>(seatIds);
    } // defensive copy
}

// Orchestrator: owns theaters, indexes for search/booking, and routes requests.
public class BookingSystem {
    private final List<Theater> theaters;
    private final Map<String, Movie> moviesById;
    private final Map<String, List<Showtime>> showtimesByMovieId;
    private final Map<String, Showtime> showtimesById;
    private final Map<String, Reservation> reservationsById;

    public BookingSystem(List<Theater> theaters) {
        this.theaters = theaters;
        this.moviesById = new HashMap<>();
        this.showtimesByMovieId = new HashMap<>();
        this.showtimesById = new HashMap<>();
        this.reservationsById = new HashMap<>();

        for (Theater theater : theaters) {
            for (Showtime showtime : theater.getShowtimes()) {
                Movie movie = showtime.getMovie();
                moviesById.put(movie.getId(), movie);
                showtimesById.put(showtime.getId(), showtime);

                showtimesByMovieId
                        .computeIfAbsent(movie.getId(), k -> new ArrayList<>())
                        .add(showtime);
            }
        }
    }

    public List<Showtime> searchMovies(String title) {
        if (title == null || title.isEmpty()) {
            return new ArrayList<>();
        }

        List<Showtime> results = new ArrayList<>();
        String searchLower = title.toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        for (Movie movie : moviesById.values()) {
            if (movie.getTitle().toLowerCase().contains(searchLower)) {
                List<Showtime> movieShowtimes = showtimesByMovieId.get(movie.getId());
                if (movieShowtimes != null) {
                    for (Showtime showtime : movieShowtimes) {
                        if (showtime.getDatetime().isAfter(now)) {
                            results.add(showtime);
                        }
                    }
                }
            }
        }

        return results;
    }

    public List<Showtime> getShowtimesAtTheater(Theater theater) {
        if (theater == null) {
            return new ArrayList<>();
        }

        List<Showtime> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Showtime showtime : theater.getShowtimes()) {
            if (showtime.getDatetime().isAfter(now)) {
                results.add(showtime);
            }
        }

        return results;
    }

    public Reservation book(String showtimeId, List<String> seatIds) {
        if (showtimeId == null || seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid booking request");
        }

        Showtime showtime = showtimesById.get(showtimeId);
        if (showtime == null) {
            throw new NoSuchElementException("Showtime not found: " + showtimeId);
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                showtime,
                seatIds);

        showtime.book(reservation);

        reservationsById.put(reservation.getConfirmationId(), reservation);

        return reservation;
    }

    public void cancelReservation(String confirmationId) {
        if (confirmationId == null || confirmationId.isEmpty()) {
            throw new IllegalArgumentException("Invalid confirmation ID");
        }

        Reservation reservation = reservationsById.get(confirmationId);
        if (reservation == null) {
            throw new NoSuchElementException("Reservation not found: " + confirmationId);
        }

        Showtime showtime = reservation.getShowtime();
        showtime.cancel(reservation);

        reservationsById.remove(confirmationId);
    }

    // Not part of the article - exercises the 4 scenarios from its
    // Verification section to confirm this implementation matches the spec.
    public static void main(String[] args) throws InterruptedException {
        Movie inception = new Movie("movie-1", "Inception");
        Theater amc = new Theater("theater-1", "AMC");
        Showtime showtime = new Showtime("showtime-123", amc, inception, LocalDateTime.now().plusHours(2), "Screen 3");
        amc.addShowtime(showtime);

        BookingSystem bookingSystem = new BookingSystem(List.of(amc));

        // Test 1: successful booking
        Reservation r1 = bookingSystem.book("showtime-123", List.of("A5", "A6"));
        System.out.println("Test 1 - booked: " + r1.getConfirmationId() + " seats=" + r1.getSeatIds());

        // Test 2: concurrent booking of the same seat - exactly one should succeed
        Showtime raceShowtime = new Showtime("showtime-race", amc, inception, LocalDateTime.now().plusHours(3),
                "Screen 4");
        amc.addShowtime(raceShowtime);
        BookingSystem raceSystem = new BookingSystem(List.of(amc));

        int[] successCount = { 0 };
        int[] failureCount = { 0 };
        Runnable bookSeatA5 = () -> {
            try {
                raceSystem.book("showtime-race", List.of("A5"));
                synchronized (successCount) {
                    successCount[0]++;
                }
            } catch (IllegalStateException e) {
                synchronized (failureCount) {
                    failureCount[0]++;
                }
            }
        };
        Thread threadA = new Thread(bookSeatA5);
        Thread threadB = new Thread(bookSeatA5);
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        System.out.println(
                "Test 2 - successes=" + successCount[0] + " failures=" + failureCount[0] + " (expect 1 and 1)");

        // Test 3: cancellation releases seats
        bookingSystem.cancelReservation(r1.getConfirmationId());
        System.out.println("Test 3 - A5 available after cancel: " + showtime.isAvailable("A5"));

        // Test 4: partial booking fails atomically
        bookingSystem.book("showtime-123", List.of("A6")); // claim A6 first
        try {
            bookingSystem.book("showtime-123", List.of("A5", "A6", "A7"));
            System.out.println("Test 4 - FAILED, booking should have thrown");
        } catch (IllegalStateException e) {
            boolean a5StillFree = showtime.isAvailable("A5");
            System.out.println("Test 4 - rejected as expected, A5 still free: " + a5StillFree);
        }
    }
}
