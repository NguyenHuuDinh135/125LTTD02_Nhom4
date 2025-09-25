package com.nhom4.moviereservation.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nhom4.moviereservation.model.Booking;
import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.MovieGenre;
import com.nhom4.moviereservation.model.MovieGenreId;
import com.nhom4.moviereservation.model.MovieRole;
import com.nhom4.moviereservation.model.MovieRoleId;
import com.nhom4.moviereservation.model.Payment;
import com.nhom4.moviereservation.model.Role;
import com.nhom4.moviereservation.model.Show;
import com.nhom4.moviereservation.model.Theater;
import com.nhom4.moviereservation.model.TheaterSeat;
import com.nhom4.moviereservation.model.TheaterSeatId;
import com.nhom4.moviereservation.model.User;
import com.nhom4.moviereservation.model.enums.BookingStatus;
import com.nhom4.moviereservation.model.enums.MovieType;
import com.nhom4.moviereservation.model.enums.PaymentMethod;
import com.nhom4.moviereservation.model.enums.RoleType;
import com.nhom4.moviereservation.model.enums.SeatType;
import com.nhom4.moviereservation.model.enums.ShowStatus;
import com.nhom4.moviereservation.model.enums.ShowType;
import com.nhom4.moviereservation.model.enums.TheaterType;
import com.nhom4.moviereservation.model.enums.UserRole;
import com.nhom4.moviereservation.repository.*;

// import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;


@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Autowired
    private MovieRoleRepository movieRoleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterSeatRepository theaterSeatRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers();
            seedGenres();
            seedMovies();
            seedRoles();
            seedMovieGenres();
            seedMovieRoles();
            seedTheaters();
            seedShows();
            seedBookings();
            seedPayments();
            seedTheaterSeats();
            
            System.out.println("Database seeded successfully!");
        } else {
            System.out.println("Database already contains data. Skipping seeding.");
        }
    }
    
    private void seedUsers() {
    List<User> users = Arrays.asList(
        createUser( "Nguyen Van A", "user1@example.com", "password123", "Hà Nội", "0912345678", UserRole.ADMIN),
        createUser("Tran Thi B", "user2@example.com", "password123", "Hồ Chí Minh", "0987654321", UserRole.API_USER),
        createUser("Le Van C", "user3@example.com", "password123", "Đà Nẵng", "0911112222", UserRole.API_USER),
        createUser("Pham Thi D", "user4@example.com", "password123", "Cần Thơ", "0903334444", UserRole.API_USER),
        createUser("Hoang Van E", "user5@example.com", "password123", "Hải Phòng", "0935556666", UserRole.API_USER),
        createUser("Nguyen Thi F", "user6@example.com", "password123", "Huế", "0927778888", UserRole.API_USER),
        createUser("Tran Van G", "user7@example.com", "password123", "Nha Trang", "0919990000", UserRole.API_USER),
        createUser("Le Thi H", "user8@example.com", "password123", "Vũng Tàu", "0988889999", UserRole.API_USER),
        createUser("Pham Van I", "user9@example.com", "password123", "Quảng Ninh", "0912223333", UserRole.API_USER),
        createUser("Hoang Thi J", "user10@example.com", "password123", "Đà Lạt", "0904445555", UserRole.API_USER)
    );
    userRepository.saveAll(users);
    System.out.println("Seeded " + users.size() + " users");
    }

    private User createUser(String fullName, String email, String password, String address, String contact, UserRole role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setAddress(address);
        user.setContact(contact);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private void seedGenres() {
        List<Genre> genres = Arrays.asList(
            createGenre(1L, "Action"),
            createGenre(2L, "Drama"),
            createGenre(3L, "Comedy"),
            createGenre(4L, "Romance"),
            createGenre(5L, "Horror"),
            createGenre(6L, "Sci-Fi"),
            createGenre(7L, "Thriller"),
            createGenre(8L, "Adventure"),
            createGenre(9L, "Fantasy"),
            createGenre(10L, "Animation")
        );
        genreRepository.saveAll(genres);
        System.out.println("Seeded " + genres.size() + " genres");
    }
    
    private Genre createGenre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setGenre(name);
        genre.setGenre(name);
        return genre;
    }

    private void seedMovies() {
        List<Movie> movies = Arrays.asList(
            createMovie(1L, "GODZILLA VS KONG", "Legends collide as Godzilla and Kong clash in an epic battle.", 2021, new BigDecimal("6.3"), "https://trailer.com/godzilla-vs-kong", "https://poster.com/godzilla-vs-kong.jpg", MovieType.ComingSoon),
            createMovie(2L, "JOKER", "Arthur Fleck, a party clown, leads an impoverished life with his ailing mother, embracing a life of crime and chaos.", 2019, new BigDecimal("8.4"), "https://trailer.com/joker", "https://poster.com/joker.jpg", MovieType.NowShowing),
            createMovie(3L, "THE BATMAN", "The Riddler plays a deadly game of cat and mouse with Batman and Commissioner Gordon in Gotham City.", 2022, new BigDecimal("7.8"), "https://trailer.com/the-batman", "https://poster.com/the-batman.jpg", MovieType.NowShowing),
            createMovie(4L, "DEADPOOL", "A wisecracking mercenary with accelerated healing powers fights for justice and revenge.", 2016, new BigDecimal("8.0"), "https://trailer.com/deadpool", "https://poster.com/deadpool.jpg", MovieType.Removed),
            createMovie(5L, "AVENGERS: ENDGAME", "The Avengers assemble to undo Thanos' actions and restore order to the universe.", 2019, new BigDecimal("8.4"), "https://trailer.com/avengers-endgame", "https://poster.com/avengers-endgame.jpg", MovieType.NowShowing),
            createMovie(6L, "THE GODFATHER", "The aging patriarch of an organized crime dynasty transfers control to his reluctant son.", 1972, new BigDecimal("9.2"), "https://trailer.com/the-godfather", "https://poster.com/the-godfather.jpg", MovieType.Removed),
            createMovie(7L, "SPIDER-MAN: NO WAY HOME", "Spider-Man's identity is revealed, bringing his superhero responsibilities into conflict with his normal life.", 2021, new BigDecimal("8.2"), "https://trailer.com/spider-man-no-way-home", "https://poster.com/spider-man-no-way-home.jpg", MovieType.Removed),
            createMovie(8L, "DUNE", "A noble family becomes embroiled in a war for control over the galaxy's most valuable asset.", 2021, new BigDecimal("8.0"), "https://trailer.com/dune", "https://poster.com/dune.jpg", MovieType.ComingSoon),
            createMovie(9L, "PARASITE", "Greed and class discrimination threaten the newly formed symbiotic relationship between two families.", 2019, new BigDecimal("8.5"), "https://trailer.com/parasite", "https://poster.com/parasite.jpg", MovieType.NowShowing),
            createMovie(10L, "INTERSTELLAR", "A team of explorers travel through a wormhole in space to ensure humanity's survival.", 2014, new BigDecimal("8.7"), "https://trailer.com/interstellar", "https://poster.com/interstellar.jpg", MovieType.Removed)
        );
        movieRepository.saveAll(movies);
        System.out.println("Seeded " + movies.size() + " movies");
    }
    
    private Movie createMovie(Long id, String title, String summary, Integer year, BigDecimal rating, String trailerUrl, String posterUrl, MovieType movieType) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setSummary(summary);
        movie.setYear(year);
        movie.setRating(rating);
        movie.setTrailerUrl(trailerUrl);
        movie.setPosterUrl(posterUrl);
        movie.setMovieType(movieType);
        return movie;
    }

    private void seedRoles() {
        List<Role> roles = Arrays.asList(
            createRole(1L, "Robert Downey Jr.", (short) 60, "https://example.com/rdj.jpg"),
            createRole(2L, "Christopher Nolan", (short) 55, "https://example.com/nolan.jpg"),
            createRole(3L, "Scarlett Johansson", (short) 40, "https://example.com/scarlett.jpg"),
            createRole(4L, "James Cameron", (short) 70, "https://example.com/cameron.jpg"),
            createRole(5L, "Ryan Reynolds", (short) 48, "https://example.com/ryan.jpg"),
            createRole(6L, "Tom Hanks", (short) 68, "https://example.com/tom.jpg"),
            createRole(7L, "Brad Pitt", (short) 61, "https://example.com/brad.jpg"),
            createRole(8L, "Morgan Freeman", (short) 88, "https://example.com/morgan.jpg"),
            createRole(9L, "Martin Scorsese", (short) 82, "https://example.com/scorsese.jpg"),
            createRole(10L, "Leonardo DiCaprio", (short) 50, "https://example.com/leo.jpg")
        );
        roleRepository.saveAll(roles);
        System.out.println("Seeded " + roles.size() + " roles");
    }
    
    private Role createRole(Long id, String fullName, Short age, String pictureUrl) {
        Role role = new Role();
        role.setId(id);
        role.setFullName(fullName);
        role.setAge(age);
        role.setPictureUrl(pictureUrl);
        return role;
    }

    private void seedMovieGenres() {
        Movie movie1 = movieRepository.findById(1L).orElse(null);
        Movie movie2 = movieRepository.findById(2L).orElse(null);
        Movie movie3 = movieRepository.findById(3L).orElse(null);
        Movie movie4 = movieRepository.findById(4L).orElse(null);
        Movie movie5 = movieRepository.findById(5L).orElse(null);
        Movie movie6 = movieRepository.findById(6L).orElse(null);
        Movie movie7 = movieRepository.findById(7L).orElse(null);
        Movie movie8 = movieRepository.findById(8L).orElse(null);
        Movie movie9 = movieRepository.findById(9L).orElse(null);
        Movie movie10 = movieRepository.findById(10L).orElse(null);
        Genre genre1 = genreRepository.findById(1L).orElse(null); // Action
        Genre genre2 = genreRepository.findById(2L).orElse(null); // Drama
        Genre genre3 = genreRepository.findById(3L).orElse(null); // Comedy
        Genre genre4 = genreRepository.findById(4L).orElse(null); // Romance
        Genre genre5 = genreRepository.findById(5L).orElse(null); // Horror
        Genre genre6 = genreRepository.findById(6L).orElse(null); // Sci-Fi
        Genre genre7 = genreRepository.findById(7L).orElse(null); // Thriller
        Genre genre8 = genreRepository.findById(8L).orElse(null); // Adventure
        Genre genre9 = genreRepository.findById(9L).orElse(null); // Fantasy
        Genre genre10 = genreRepository.findById(10L).orElse(null); // Animation
    
        List<MovieGenre> movieGenres = Arrays.asList(
            createMovieGenre(movie1, genre1), // GODZILLA VS KONG - Action
            createMovieGenre(movie1, genre8), // GODZILLA VS KONG - Adventure
            createMovieGenre(movie2, genre2), // JOKER - Drama
            createMovieGenre(movie2, genre7), // JOKER - Thriller
            createMovieGenre(movie3, genre1), // THE BATMAN - Action
            createMovieGenre(movie3, genre7), // THE BATMAN - Thriller
            createMovieGenre(movie4, genre1), // DEADPOOL - Action
            createMovieGenre(movie4, genre3), // DEADPOOL - Comedy
            createMovieGenre(movie5, genre1), // AVENGERS: ENDGAME - Action
            createMovieGenre(movie5, genre6), // AVENGERS: ENDGAME - Sci-Fi
            createMovieGenre(movie6, genre2), // THE GODFATHER - Drama
            createMovieGenre(movie7, genre1), // SPIDER-MAN: NO WAY HOME - Action
            createMovieGenre(movie7, genre9), // SPIDER-MAN: NO WAY HOME - Fantasy
            createMovieGenre(movie8, genre6), // DUNE - Sci-Fi
            createMovieGenre(movie8, genre8), // DUNE - Adventure
            createMovieGenre(movie9, genre2), // PARASITE - Drama
            createMovieGenre(movie9, genre7), // PARASITE - Thriller
            createMovieGenre(movie10, genre6), // INTERSTELLAR - Sci-Fi
            createMovieGenre(movie10, genre2)  // INTERSTELLAR - Drama
        );
        movieGenreRepository.saveAll(movieGenres);
        System.out.println("Seeded " + movieGenres.size() + " movieGenres");
    }
    
    private MovieGenre createMovieGenre(Movie movie, Genre genre) {
        if (movie == null || genre == null) {
            return null; // Bỏ qua cặp không hợp lệ
        }
        MovieGenre movieGenre = new MovieGenre();
        movieGenre.setId(new MovieGenreId(movie.getId(), genre.getId()));
        movieGenre.setMovie(movie);
        movieGenre.setGenre(genre);
        return movieGenre;
    }

    private void seedMovieRoles() {
    Movie movie1 = movieRepository.findById(1L).orElse(null); // GODZILLA VS KONG
    Movie movie2 = movieRepository.findById(2L).orElse(null); // JOKER
    Movie movie3 = movieRepository.findById(3L).orElse(null); // THE BATMAN
    Movie movie4 = movieRepository.findById(4L).orElse(null); // DEADPOOL
    Movie movie5 = movieRepository.findById(5L).orElse(null); // AVENGERS: ENDGAME
    Movie movie6 = movieRepository.findById(6L).orElse(null); // THE GODFATHER
    Movie movie7 = movieRepository.findById(7L).orElse(null); // SPIDER-MAN: NO WAY HOME
    Movie movie8 = movieRepository.findById(8L).orElse(null); // DUNE
    Movie movie9 = movieRepository.findById(9L).orElse(null); // PARASITE
    Movie movie10 = movieRepository.findById(10L).orElse(null); // INTERSTELLAR
    Role role1 = roleRepository.findById(1L).orElse(null); // Robert Downey Jr.
    Role role2 = roleRepository.findById(2L).orElse(null); // Christopher Nolan
    Role role3 = roleRepository.findById(3L).orElse(null); // Scarlett Johansson
    Role role4 = roleRepository.findById(4L).orElse(null); // James Cameron
    Role role5 = roleRepository.findById(5L).orElse(null); // Ryan Reynolds
    Role role6 = roleRepository.findById(6L).orElse(null); // Tom Hanks
    Role role7 = roleRepository.findById(7L).orElse(null); // Brad Pitt
    Role role8 = roleRepository.findById(8L).orElse(null); // Morgan Freeman
    Role role9 = roleRepository.findById(9L).orElse(null); // Martin Scorsese
    Role role10 = roleRepository.findById(10L).orElse(null); // Leonardo DiCaprio

    List<MovieRole> movieRoles = Arrays.asList(
        createMovieRole(movie1, role4, RoleType.Director), // GODZILLA VS KONG - James Cameron (Director)
        createMovieRole(movie1, role5, RoleType.Cast), // GODZILLA VS KONG - Ryan Reynolds (Cast)
        createMovieRole(movie2, role2, RoleType.Director), // JOKER - Christopher Nolan (Director)
        createMovieRole(movie2, role10, RoleType.Cast), // JOKER - Leonardo DiCaprio (Cast)
        createMovieRole(movie3, role2, RoleType.Director), // THE BATMAN - Christopher Nolan (Director)
        createMovieRole(movie3, role3, RoleType.Cast), // THE BATMAN - Scarlett Johansson (Cast)
        createMovieRole(movie4, role5, RoleType.Cast), // DEADPOOL - Ryan Reynolds (Cast)
        createMovieRole(movie4, role4, RoleType.Director), // DEADPOOL - James Cameron (Director)
        createMovieRole(movie5, role1, RoleType.Cast), // AVENGERS: ENDGAME - Robert Downey Jr. (Cast)
        createMovieRole(movie5, role3, RoleType.Cast), // AVENGERS: ENDGAME - Scarlett Johansson (Cast)
        createMovieRole(movie6, role9, RoleType.Director), // THE GODFATHER - Martin Scorsese (Director)
        createMovieRole(movie6, role6, RoleType.Cast), // THE GODFATHER - Tom Hanks (Cast)
        createMovieRole(movie7, role1, RoleType.Cast), // SPIDER-MAN: NO WAY HOME - Robert Downey Jr. (Cast)
        createMovieRole(movie7, role2, RoleType.Director), // SPIDER-MAN: NO WAY HOME - Christopher Nolan (Director)
        createMovieRole(movie8, role4, RoleType.Director), // DUNE - James Cameron (Director)
        createMovieRole(movie8, role7, RoleType.Cast), // DUNE - Brad Pitt (Cast)
        createMovieRole(movie9, role9, RoleType.Director), // PARASITE - Martin Scorsese (Director)
        createMovieRole(movie9, role10, RoleType.Cast), // PARASITE - Leonardo DiCaprio (Cast)
        createMovieRole(movie10, role2, RoleType.Director), // INTERSTELLAR - Christopher Nolan (Director)
        createMovieRole(movie10, role8, RoleType.Cast) // INTERSTELLAR - Morgan Freeman (Cast)
    ).stream().filter(Objects::nonNull).collect(Collectors.toList());
    movieRoleRepository.saveAll(movieRoles);
    System.out.println("Seeded " + movieRoles.size() + " movieRoles");
}

    private MovieRole createMovieRole(Movie movie, Role role, RoleType roleType) {
        if (movie == null || role == null) {
            return null; // Bỏ qua cặp không hợp lệ
        }
        MovieRole movieRole = new MovieRole();
        movieRole.setId(new MovieRoleId(movie.getId(), role.getId()));
        movieRole.setMovie(movie);
        movieRole.setRole(role);
        movieRole.setRoleType(roleType);
        return movieRole;
    }

    private void seedTheaters() {
        List<Theater> theaters = Arrays.asList(
            createTheater(1L, "Rạp CGV Vincom", TheaterType.Royal),
            createTheater(2L, "Rạp Lotte Cinema", TheaterType.Normal),
            createTheater(3L, "Rạp BHD Star", TheaterType.Royal),
            createTheater(4L, "Rạp Galaxy Nguyễn Trãi", TheaterType.Normal),
            createTheater(5L, "Rạp Quốc Gia", TheaterType.Normal)
        );
        theaterRepository.saveAll(theaters);
        System.out.println("Seeded " + theaters.size() + " theaters");
    }
    
    private Theater createTheater(Long id, String name, TheaterType type) {
        Theater theater = new Theater();
        theater.setId(id);
        theater.setName(name);
        theater.setType(type);
        return theater;
    }


    private void seedShows() {
        List<Movie> movies = movieRepository.findAll();
        List<Theater> theaters = theaterRepository.findAll();
    
        if (movies.isEmpty() || theaters.isEmpty()) {
            System.out.println("Không thể khởi tạo suất chiếu vì thiếu phim hoặc rạp");
            return;
        }
    
        List<Show> shows = Arrays.asList(
            createShow(1L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 1), movies.get(0), theaters.get(0), ShowStatus.Free, ShowType.TwoD), // GODZILLA VS KONG - CGV Vincom
            createShow(2L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 1), movies.get(1), theaters.get(1), ShowStatus.Free, ShowType.ThreeD), // JOKER - Lotte Cinema
            createShow(3L, LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 1), movies.get(2), theaters.get(2), ShowStatus.Full, ShowType.TwoD), // THE BATMAN - BHD Star
            createShow(4L, LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 1), movies.get(3), theaters.get(3), ShowStatus.Full, ShowType.ThreeD), // DEADPOOL - Galaxy Nguyễn Trãi
            createShow(5L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 2), movies.get(4), theaters.get(4), ShowStatus.AlmostFull, ShowType.TwoD), // AVENGERS: ENDGAME - Quốc Gia
            createShow(6L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 2), movies.get(5), theaters.get(0), ShowStatus.AlmostFull, ShowType.TwoD), // THE GODFATHER - CGV Vincom
            createShow(7L, LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 2), movies.get(6), theaters.get(1), ShowStatus.AlmostFull, ShowType.TwoD), // SPIDER-MAN: NO WAY HOME - Lotte Cinema
            createShow(8L, LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 2), movies.get(7), theaters.get(2), ShowStatus.Free, ShowType.ThreeD), // DUNE - BHD Star
            createShow(9L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 3), movies.get(8), theaters.get(3), ShowStatus.Full, ShowType.TwoD), // PARASITE - Galaxy Nguyễn Trãi
            createShow(10L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 3), movies.get(9), theaters.get(4), ShowStatus.Free, ShowType.ThreeD) // INTERSTELLAR - Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        showRepository.saveAll(shows);
        System.out.println("Seeded " + shows.size() + " shows");
    }
    
    private Show createShow(Long id, LocalTime startTime, LocalTime endTime, LocalDate date, Movie movie, Theater theater, ShowStatus status, ShowType type) {
        if (movie == null || theater == null) {
            return null; // Bỏ qua suất chiếu không hợp lệ
        }
        Show show = new Show();
        show.setId(id);
        show.setStartTime(startTime);
        show.setEndTime(endTime);
        show.setDate(date);
        show.setMovie(movie);
        show.setTheater(theater);
        show.setStatus(status);
        show.setType(type);
        return show;
    }

    private void seedBookings() {
        List<User> users = userRepository.findAll();
        List<Show> shows = showRepository.findAll();
    
        if (users.isEmpty() || shows.isEmpty()) {
            System.out.println("Không thể khởi tạo vé đặt chỗ vì thiếu người dùng hoặc suất chiếu");
            return;
        }
    
        List<Booking> bookings = Arrays.asList(
            createBooking(1L, users.get(0), shows.get(0), "A", 1, 100000f, BookingStatus.Reserved, LocalDateTime.of(2025, 9, 18, 17, 48)), // User 1, GODZILLA VS KONG, CGV Vincom
            createBooking(2L, users.get(1), shows.get(1), "B", 5, 120000f, BookingStatus.Confirmed, LocalDateTime.of(2025, 9, 17, 17, 48)), // User 2, JOKER, Lotte Cinema
            createBooking(3L, users.get(2), shows.get(2), "C", 10, 150000f, BookingStatus.Confirmed, LocalDateTime.of(2025, 9, 18, 15, 48)), // User 3, THE BATMAN, BHD Star
            createBooking(4L, users.get(3), shows.get(3), "D", 3, 90000f, BookingStatus.Cancelled, LocalDateTime.of(2025, 9, 16, 17, 48)), // User 4, DEADPOOL, Galaxy Nguyễn Trãi
            createBooking(5L, users.get(4), shows.get(4), "E", 7, 110000f, BookingStatus.Reserved, LocalDateTime.of(2025, 9, 18, 17, 48)) // User 5, AVENGERS: ENDGAME, Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        bookingRepository.saveAll(bookings);
        System.out.println("Seeded " + bookings.size() + " bookings");
    }
    
    private Booking createBooking(Long id, User user, Show show, String seatRow, Integer seatNumber, Float price, BookingStatus status, LocalDateTime bookingDateTime) {
        if (user == null || show == null) {
            return null; // Bỏ qua vé đặt chỗ không hợp lệ
        }
        Booking booking = new Booking();
        booking.setId(id);
        booking.setUser(user);
        booking.setShow(show);
        booking.setSeatRow(seatRow);
        booking.setSeatNumber(seatNumber);
        booking.setPrice(price);
        booking.setStatus(status);
        booking.setBookingDateTime(bookingDateTime);
        return booking;
    }

    private void seedPayments() {
        List<User> users = userRepository.findAll();
        List<Show> shows = showRepository.findAll();
    
        if (users.isEmpty() || shows.isEmpty()) {
            System.out.println("Không thể khởi tạo thanh toán vì thiếu người dùng hoặc suất chiếu");
            return;
        }
    
        List<Payment> payments = Arrays.asList(
            createPayment(1L, 100000, LocalDateTime.of(2025, 9, 18, 17, 53), PaymentMethod.Card, users.get(0), shows.get(0)), // User 1, GODZILLA VS KONG, CGV Vincom
            createPayment(2L, 120000, LocalDateTime.of(2025, 9, 18, 16, 53), PaymentMethod.Card, users.get(1), shows.get(1)), // User 2, JOKER, Lotte Cinema
            createPayment(3L, 150000, LocalDateTime.of(2025, 9, 18, 15, 53), PaymentMethod.Card, users.get(2), shows.get(2)), // User 3, THE BATMAN, BHD Star
            createPayment(4L, 90000, LocalDateTime.of(2025, 9, 18, 14, 53), PaymentMethod.Cash, users.get(3), shows.get(3)), // User 4, DEADPOOL, Galaxy Nguyễn Trãi
            createPayment(5L, 110000, LocalDateTime.of(2025, 9, 18, 13, 53), PaymentMethod.Cash, users.get(4), shows.get(4)) // User 5, AVENGERS: ENDGAME, Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        paymentRepository.saveAll(payments);
        System.out.println("Seeded " + payments.size() + " payments");
    }
    
    private Payment createPayment(Long id, Integer amount, LocalDateTime paymentDateTime, PaymentMethod method, User user, Show show) {
        if (user == null || show == null) {
            return null; // Bỏ qua thanh toán không hợp lệ
        }
        Payment payment = new Payment();
        payment.setId(id);
        payment.setAmount(amount);
        payment.setPaymentDateTime(paymentDateTime);
        payment.setMethod(method);
        payment.setUser(user);
        payment.setShow(show);
        return payment;
    }

    private void seedTheaterSeats() {
        List<Theater> theaters = theaterRepository.findAll();
    
        if (theaters.isEmpty()) {
            System.out.println("Không thể khởi tạo ghế rạp vì thiếu rạp chiếu");
            return;
        }
    
        List<TheaterSeat> theaterSeats = Arrays.asList(
            // Rạp CGV Vincom (theaters.get(0))
            createTheaterSeat("A", 1, theaters.get(0), SeatType.Normal), // Hỗ trợ Booking 1
            createTheaterSeat("A", 2, theaters.get(0), SeatType.Normal),
            createTheaterSeat("A", 3, theaters.get(0), SeatType.Royal),
            createTheaterSeat("A", 4, theaters.get(0), SeatType.Royal),
            // Rạp Lotte Cinema (theaters.get(1))
            createTheaterSeat("B", 1, theaters.get(1), SeatType.Normal),
            createTheaterSeat("B", 2, theaters.get(1), SeatType.Royal),
            createTheaterSeat("B", 5, theaters.get(1), SeatType.Normal), // Hỗ trợ Booking 2
            createTheaterSeat("B", 6, theaters.get(1), SeatType.Normal),
            // Rạp BHD Star (theaters.get(2))
            createTheaterSeat("C", 1, theaters.get(2), SeatType.Royal),
            createTheaterSeat("C", 2, theaters.get(2), SeatType.Normal),
            createTheaterSeat("C", 10, theaters.get(2), SeatType.Normal), // Hỗ trợ Booking 3
            createTheaterSeat("C", 11, theaters.get(2), SeatType.Royal),
            // Rạp Galaxy Nguyễn Trãi (theaters.get(3))
            createTheaterSeat("D", 1, theaters.get(3), SeatType.Normal),
            createTheaterSeat("D", 2, theaters.get(3), SeatType.Normal),
            createTheaterSeat("D", 3, theaters.get(3), SeatType.Normal), // Hỗ trợ Booking 4
            createTheaterSeat("D", 4, theaters.get(3), SeatType.Royal),
            // Rạp Quốc Gia (theaters.get(4))
            createTheaterSeat("E", 1, theaters.get(4), SeatType.Royal),
            createTheaterSeat("E", 2, theaters.get(4), SeatType.Normal),
            createTheaterSeat("E", 7, theaters.get(4), SeatType.Royal), // Hỗ trợ Booking 5
            createTheaterSeat("E", 8, theaters.get(4), SeatType.Royal)
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        theaterSeatRepository.saveAll(theaterSeats);
        System.out.println("Seeded " + theaterSeats.size() + " theaterSeats");
    }
    
    private TheaterSeat createTheaterSeat(String seatRow, Integer seatNumber, Theater theater, SeatType type) {
        if (theater == null) {
            return null; // Bỏ qua ghế không hợp lệ
        }
        TheaterSeat theaterSeat = new TheaterSeat();
        TheaterSeatId theaterSeatId = new TheaterSeatId(seatRow, seatNumber, theater.getId());
        theaterSeat.setId(theaterSeatId);
        theaterSeat.setTheater(theater);
        theaterSeat.setType(type);
        return theaterSeat;
    }

}