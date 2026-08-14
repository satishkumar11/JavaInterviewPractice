package bookmyshow.blueprint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// STRUCTURE ONLY — same classes, fields, and method signatures as
// bookmyshow.BookingSystem, but every body is a stub. No business logic here.
// In its own subpackage so it can coexist with the real implementation
// without class-name collisions.

// A film that can be showing at multiple theaters and times.
class Movie {
    private final String id;
    private final String title;

    public Movie(String id, String title) {
        this.id = null;
        this.title = null;
    }

    public String getId() {
        return null;
    }

    public String getTitle() {
        return null;
    }
}

// A physical location; owns the showtimes playing there.
class Theater {
    private final String id;
    private final String name;
    private final List<Showtime> showtimes;

    public Theater(String id, String name) {
        this.id = null;
        this.name = null;
        this.showtimes = null;
    }

    public void addShowtime(Showtime showtime) {
    }

    public List<Showtime> getShowtimes() {
        return null;
    }

    public List<Showtime> getShowtimesForMovie(Movie movie) {
        return null;
    }
}

// One bookable screening: tracks reservations and derives seat availability from them.
class Showtime {
    private final String id;
    private final Theater theater;
    private final Movie movie;
    private final LocalDateTime datetime;
    private final String screenLabel;
    private final List<Reservation> reservations;

    public Showtime(String id, Theater theater, Movie movie, LocalDateTime datetime, String screenLabel) {
        this.id = null;
        this.theater = null;
        this.movie = null;
        this.datetime = null;
        this.screenLabel = null;
        this.reservations = null;
    }

    public String getId() {
        return null;
    }

    public Theater getTheater() {
        return null;
    }

    public LocalDateTime getDatetime() {
        return null;
    }

    public Movie getMovie() {
        return null;
    }

    public boolean isAvailable(String seatId) {
        return false;
    }

    public List<String> getAvailableSeats() {
        return null;
    }

    public void book(Reservation reservation) {
    }

    public void cancel(Reservation reservation) {
    }

    private boolean isValidSeatId(String seatId) {
        return false;
    }
}

// A user's booking record; used to route cancellations back to the right showtime.
class Reservation {
    private final String confirmationId;
    private final Showtime showtime;
    private final List<String> seatIds;

    public Reservation(String confirmationId, Showtime showtime, List<String> seatIds) {
        this.confirmationId = null;
        this.showtime = null;
        this.seatIds = null;
    }

    public String getConfirmationId() {
        return null;
    }

    public Showtime getShowtime() {
        return null;
    }

    public List<String> getSeatIds() {
        return null;
    }
}

// Orchestrator: owns theaters, indexes for search/booking, and routes requests.
public class BookingSystem {
    private final List<Theater> theaters;
    private final Map<String, Movie> moviesById;
    private final Map<String, List<Showtime>> showtimesByMovieId;
    private final Map<String, Showtime> showtimesById;
    private final Map<String, Reservation> reservationsById;

    public BookingSystem(List<Theater> theaters) {
        this.theaters = null;
        this.moviesById = null;
        this.showtimesByMovieId = null;
        this.showtimesById = null;
        this.reservationsById = null;
    }

    public List<Showtime> searchMovies(String title) {
        return null;
    }

    public List<Showtime> getShowtimesAtTheater(Theater theater) {
        return null;
    }

    public Reservation book(String showtimeId, List<String> seatIds) {
        return null;
    }

    public void cancelReservation(String confirmationId) {
    }
}
