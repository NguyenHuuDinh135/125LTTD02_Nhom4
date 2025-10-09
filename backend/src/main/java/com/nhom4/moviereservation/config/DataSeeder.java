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
import org.springframework.stereotype.Component;

import com.nhom4.moviereservation.Movie.MovieRepository;
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
import com.nhom4.moviereservation.repository.BookingRepository;
import com.nhom4.moviereservation.repository.GenreRepository;
import com.nhom4.moviereservation.repository.MovieGenreRepository;
import com.nhom4.moviereservation.repository.MovieRoleRepository;
import com.nhom4.moviereservation.repository.PaymentRepository;
import com.nhom4.moviereservation.repository.RoleRepository;
import com.nhom4.moviereservation.repository.ShowRepository;
import com.nhom4.moviereservation.repository.TheaterRepository;
import com.nhom4.moviereservation.repository.TheaterSeatRepository;
import com.nhom4.moviereservation.repository.UserRepository;

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
            seedTheaterSeats();
            seedBookings();
            seedPayments();
            
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

    // private User createUser(String fullName, String email, String password, String address, String contact, UserRole role) {
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
            createGenre("Action"),
            createGenre("Drama"),
            createGenre("Comedy"),
            createGenre("Romance"),
            createGenre("Horror"),
            createGenre("Sci-Fi"),
            createGenre("Thriller"),
            createGenre("Adventure"),
            createGenre("Fantasy"),
            createGenre("Animation")
        );
        List<Genre> savedGenres = genreRepository.saveAll(genres);
        System.out.println("Seeded " + savedGenres.size() + " genres");
    }
    
    private Genre createGenre(String name) {
        Genre genre = new Genre();
        genre.setGenre(name);
        return genre;
    }

    private void seedMovies() {
        List<Movie> movies = Arrays.asList(
            createMovie("GODZILLA VS KONG", "Legends collide as Godzilla and Kong clash in an epic battle.", 2021, new BigDecimal("6.3"), "https://trailer.com/godzilla-vs-kong  ", "https://poster.com/godzilla-vs-kong.jpg  ", MovieType.ComingSoon),
            createMovie("JOKER", "Arthur Fleck, a party clown, leads an impoverished life with his ailing mother, embracing a life of crime and chaos.", 2019, new BigDecimal("8.4"), "https://trailer.com/joker  ", "https://poster.com/joker.jpg  ", MovieType.NowShowing),
            createMovie("THE BATMAN", "The Riddler plays a deadly game of cat and mouse with Batman and Commissioner Gordon in Gotham City.", 2022, new BigDecimal("7.8"), "https://trailer.com/the-batman  ", "https://poster.com/the-batman.jpg  ", MovieType.NowShowing),
            createMovie("DEADPOOL", "A wisecracking mercenary with accelerated healing powers fights for justice and revenge.", 2016, new BigDecimal("8.0"), "https://trailer.com/deadpool  ", "https://poster.com/deadpool.jpg  ", MovieType.Removed),
            createMovie("AVENGERS: ENDGAME", "The Avengers assemble to undo Thanos' actions and restore order to the universe.", 2019, new BigDecimal("8.4"), "https://trailer.com/avengers-endgame  ", "https://poster.com/avengers-endgame.jpg  ", MovieType.NowShowing),
            createMovie("THE GODFATHER", "The aging patriarch of an organized crime dynasty transfers control to his reluctant son.", 1972, new BigDecimal("9.2"), "https://trailer.com/the-godfather  ", "https://poster.com/the-godfather.jpg  ", MovieType.Removed),
            createMovie("SPIDER-MAN: NO WAY HOME", "Spider-Man's identity is revealed, bringing his superhero responsibilities into conflict with his normal life.", 2021, new BigDecimal("8.2"), "https://trailer.com/spider-man-no-way-home  ", "https://poster.com/spider-man-no-way-home.jpg  ", MovieType.Removed),
            createMovie("DUNE", "A noble family becomes embroiled in a war for control over the galaxy's most valuable asset.", 2021, new BigDecimal("8.0"), "https://trailer.com/dune  ", "https://poster.com/dune.jpg  ", MovieType.ComingSoon),
            createMovie("PARASITE", "Greed and class discrimination threaten the newly formed symbiotic relationship between two families.", 2019, new BigDecimal("8.5"), "https://trailer.com/parasite  ", "https://poster.com/parasite.jpg  ", MovieType.NowShowing),
            createMovie("INTERSTELLAR", "A team of explorers travel through a wormhole in space to ensure humanity's survival.", 2014, new BigDecimal("8.7"), "https://trailer.com/interstellar  ", "https://poster.com/interstellar.jpg  ", MovieType.Removed)
        );
        List<Movie> savedMovies = movieRepository.saveAll(movies);
        System.out.println("Seeded " + savedMovies.size() + " movies");
    }
    
    private Movie createMovie(String title, String summary, Integer year, BigDecimal rating, String trailerUrl, String posterUrl, MovieType movieType) {
        Movie movie = new Movie();
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
            createRole("Robert Downey Jr.", (short) 60, "https://example.com/rdj.jpg  "),
            createRole("Christopher Nolan", (short) 55, "https://example.com/nolan.jpg  "),
            createRole("Scarlett Johansson", (short) 40, "https://example.com/scarlett.jpg  "),
            createRole("James Cameron", (short) 70, "https://example.com/cameron.jpg  "),
            createRole("Ryan Reynolds", (short) 48, "https://example.com/ryan.jpg  "),
            createRole("Tom Hanks", (short) 68, "https://example.com/tom.jpg  "),
            createRole("Brad Pitt", (short) 61, "https://example.com/brad.jpg  "),
            createRole("Morgan Freeman", (short) 88, "https://example.com/morgan.jpg  "),
            createRole("Martin Scorsese", (short) 82, "https://example.com/scorsese.jpg  "),
            createRole("Leonardo DiCaprio", (short) 50, "https://example.com/leo.jpg  ")
        );
        List<Role> savedRoles = roleRepository.saveAll(roles);
        System.out.println("Seeded " + savedRoles.size() + " roles");
    }
    
    private Role createRole(String fullName, Short age, String pictureUrl) {
        Role role = new Role();
        role.setFullName(fullName);
        role.setAge(age);
        role.setPictureUrl(pictureUrl);
        return role;
    }

    private void seedMovieGenres() {
        List<Movie> movies = movieRepository.findAll();
        List<Genre> genres = genreRepository.findAll();
    
        if (movies.isEmpty() || genres.isEmpty()) {
            System.out.println("Không thể khởi tạo thể loại phim vì thiếu phim hoặc thể loại");
            return;
        }
    
        List<MovieGenre> movieGenres = Arrays.asList(
            createMovieGenre(movies.get(0), genres.get(0)), // GODZILLA VS KONG - Action
            createMovieGenre(movies.get(0), genres.get(7)), // GODZILLA VS KONG - Adventure
            createMovieGenre(movies.get(1), genres.get(1)), // JOKER - Drama
            createMovieGenre(movies.get(1), genres.get(6)), // JOKER - Thriller
            createMovieGenre(movies.get(2), genres.get(0)), // THE BATMAN - Action
            createMovieGenre(movies.get(2), genres.get(6)), // THE BATMAN - Thriller
            createMovieGenre(movies.get(3), genres.get(0)), // DEADPOOL - Action
            createMovieGenre(movies.get(3), genres.get(2)), // DEADPOOL - Comedy
            createMovieGenre(movies.get(4), genres.get(0)), // AVENGERS: ENDGAME - Action
            createMovieGenre(movies.get(4), genres.get(5)), // AVENGERS: ENDGAME - Sci-Fi
            createMovieGenre(movies.get(5), genres.get(1)), // THE GODFATHER - Drama
            createMovieGenre(movies.get(6), genres.get(0)), // SPIDER-MAN: NO WAY HOME - Action
            createMovieGenre(movies.get(6), genres.get(8)), // SPIDER-MAN: NO WAY HOME - Fantasy
            createMovieGenre(movies.get(7), genres.get(5)), // DUNE - Sci-Fi
            createMovieGenre(movies.get(7), genres.get(7)), // DUNE - Adventure
            createMovieGenre(movies.get(8), genres.get(1)), // PARASITE - Drama
            createMovieGenre(movies.get(8), genres.get(6)), // PARASITE - Thriller
            createMovieGenre(movies.get(9), genres.get(5)), // INTERSTELLAR - Sci-Fi
            createMovieGenre(movies.get(9), genres.get(1))  // INTERSTELLAR - Drama
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
        List<Movie> movies = movieRepository.findAll();
        List<Role> roles = roleRepository.findAll();
    
        if (movies.isEmpty() || roles.isEmpty()) {
            System.out.println("Không thể khởi tạo vai diễn phim vì thiếu phim hoặc vai diễn");
            return;
        }
    
        List<MovieRole> movieRoles = Arrays.asList(
            createMovieRole(movies.get(0), roles.get(3), RoleType.Director), // GODZILLA VS KONG - James Cameron (Director)
            createMovieRole(movies.get(0), roles.get(4), RoleType.Cast), // GODZILLA VS KONG - Ryan Reynolds (Cast)
            createMovieRole(movies.get(1), roles.get(1), RoleType.Director), // JOKER - Christopher Nolan (Director)
            createMovieRole(movies.get(1), roles.get(9), RoleType.Cast), // JOKER - Leonardo DiCaprio (Cast)
            createMovieRole(movies.get(2), roles.get(1), RoleType.Director), // THE BATMAN - Christopher Nolan (Director)
            createMovieRole(movies.get(2), roles.get(2), RoleType.Cast), // THE BATMAN - Scarlett Johansson (Cast)
            createMovieRole(movies.get(3), roles.get(4), RoleType.Cast), // DEADPOOL - Ryan Reynolds (Cast)
            createMovieRole(movies.get(3), roles.get(3), RoleType.Director), // DEADPOOL - James Cameron (Director)
            createMovieRole(movies.get(4), roles.get(0), RoleType.Cast), // AVENGERS: ENDGAME - Robert Downey Jr. (Cast)
            createMovieRole(movies.get(4), roles.get(2), RoleType.Cast), // AVENGERS: ENDGAME - Scarlett Johansson (Cast)
            createMovieRole(movies.get(5), roles.get(8), RoleType.Director), // THE GODFATHER - Martin Scorsese (Director)
            createMovieRole(movies.get(5), roles.get(5), RoleType.Cast), // THE GODFATHER - Tom Hanks (Cast)
            createMovieRole(movies.get(6), roles.get(0), RoleType.Cast), // SPIDER-MAN: NO WAY HOME - Robert Downey Jr. (Cast)
            createMovieRole(movies.get(6), roles.get(1), RoleType.Director), // SPIDER-MAN: NO WAY HOME - Christopher Nolan (Director)
            createMovieRole(movies.get(7), roles.get(3), RoleType.Director), // DUNE - James Cameron (Director)
            createMovieRole(movies.get(7), roles.get(6), RoleType.Cast), // DUNE - Brad Pitt (Cast)
            createMovieRole(movies.get(8), roles.get(8), RoleType.Director), // PARASITE - Martin Scorsese (Director)
            createMovieRole(movies.get(8), roles.get(9), RoleType.Cast), // PARASITE - Leonardo DiCaprio (Cast)
            createMovieRole(movies.get(9), roles.get(1), RoleType.Director), // INTERSTELLAR - Christopher Nolan (Director)
            createMovieRole(movies.get(9), roles.get(7), RoleType.Cast) // INTERSTELLAR - Morgan Freeman (Cast)
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
            createTheater("Rạp CGV Vincom", TheaterType.Royal),
            createTheater("Rạp Lotte Cinema", TheaterType.Normal),
            createTheater("Rạp BHD Star", TheaterType.Royal),
            createTheater("Rạp Galaxy Nguyễn Trãi", TheaterType.Normal),
            createTheater("Rạp Quốc Gia", TheaterType.Normal)
        );
        List<Theater> savedTheaters = theaterRepository.saveAll(theaters);
        System.out.println("Seeded " + savedTheaters.size() + " theaters");
    }
    
    private Theater createTheater(String name, TheaterType type) {
        Theater theater = new Theater();
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
            createShow(LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 1), movies.get(0), theaters.get(0), ShowStatus.Free, ShowType.TwoD), // GODZILLA VS KONG - CGV Vincom
            createShow(LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 1), movies.get(1), theaters.get(1), ShowStatus.Free, ShowType.ThreeD), // JOKER - Lotte Cinema
            createShow(LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 1), movies.get(2), theaters.get(2), ShowStatus.Full, ShowType.TwoD), // THE BATMAN - BHD Star
            createShow(LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 1), movies.get(3), theaters.get(3), ShowStatus.Full, ShowType.ThreeD), // DEADPOOL - Galaxy Nguyễn Trãi
            createShow(LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 2), movies.get(4), theaters.get(4), ShowStatus.AlmostFull, ShowType.TwoD), // AVENGERS: ENDGAME - Quốc Gia
            createShow(LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 2), movies.get(5), theaters.get(0), ShowStatus.AlmostFull, ShowType.TwoD), // THE GODFATHER - CGV Vincom
            createShow(LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 2), movies.get(6), theaters.get(1), ShowStatus.AlmostFull, ShowType.TwoD), // SPIDER-MAN: NO WAY HOME - Lotte Cinema
            createShow(LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 2), movies.get(7), theaters.get(2), ShowStatus.Free, ShowType.ThreeD), // DUNE - BHD Star
            createShow(LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 3), movies.get(8), theaters.get(3), ShowStatus.Full, ShowType.TwoD), // PARASITE - Galaxy Nguyễn Trãi
            createShow(LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 3), movies.get(9), theaters.get(4), ShowStatus.Free, ShowType.ThreeD) // INTERSTELLAR - Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<Show> savedShows = showRepository.saveAll(shows);
        System.out.println("Seeded " + savedShows.size() + " shows");
    }
    
    private Show createShow(LocalTime startTime, LocalTime endTime, LocalDate date, Movie movie, Theater theater, ShowStatus status, ShowType type) {
        if (movie == null || theater == null) {
            return null; // Bỏ qua suất chiếu không hợp lệ
        }
        Show show = new Show();
        show.setStartTime(startTime);
        show.setEndTime(endTime);
        show.setDate(date);
        show.setMovie(movie);
        show.setTheater(theater);
        show.setStatus(status);
        show.setType(type);
        return show;
    }

    private void seedTheaterSeats() {
        List<Theater> theaters = theaterRepository.findAll();
    
        if (theaters.isEmpty()) {
            System.out.println("Không thể khởi tạo ghế rạp vì thiếu rạp chiếu");
            return;
        }
    
        List<TheaterSeat> theaterSeats = Arrays.asList(
            // Rạp CGV Vincom (theaters.get(0))
            createTheaterSeat("A", 1, theaters.get(0), SeatType.Normal),
            createTheaterSeat("A", 2, theaters.get(0), SeatType.Normal),
            createTheaterSeat("A", 3, theaters.get(0), SeatType.Royal),
            createTheaterSeat("A", 4, theaters.get(0), SeatType.Royal),
            // Rạp Lotte Cinema (theaters.get(1))
            createTheaterSeat("B", 1, theaters.get(1), SeatType.Normal),
            createTheaterSeat("B", 2, theaters.get(1), SeatType.Royal),
            createTheaterSeat("B", 5, theaters.get(1), SeatType.Normal),
            createTheaterSeat("B", 6, theaters.get(1), SeatType.Normal),
            // Rạp BHD Star (theaters.get(2))
            createTheaterSeat("C", 1, theaters.get(2), SeatType.Royal),
            createTheaterSeat("C", 2, theaters.get(2), SeatType.Normal),
            createTheaterSeat("C", 10, theaters.get(2), SeatType.Normal),
            createTheaterSeat("C", 11, theaters.get(2), SeatType.Royal),
            // Rạp Galaxy Nguyễn Trãi (theaters.get(3))
            createTheaterSeat("D", 1, theaters.get(3), SeatType.Normal),
            createTheaterSeat("D", 2, theaters.get(3), SeatType.Normal),
            createTheaterSeat("D", 3, theaters.get(3), SeatType.Normal),
            createTheaterSeat("D", 4, theaters.get(3), SeatType.Royal),
            // Rạp Quốc Gia (theaters.get(4))
            createTheaterSeat("E", 1, theaters.get(4), SeatType.Royal),
            createTheaterSeat("E", 2, theaters.get(4), SeatType.Normal),
            createTheaterSeat("E", 7, theaters.get(4), SeatType.Royal),
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

    private void seedBookings() {
        List<User> users = userRepository.findAll();
        List<Show> shows = showRepository.findAll();
    
        if (users.isEmpty() || shows.isEmpty()) {
            System.out.println("Không thể khởi tạo vé đặt chỗ vì thiếu người dùng hoặc suất chiếu");
            return;
        }
    
        List<Booking> bookings = Arrays.asList(
            createBooking(users.get(0), shows.get(0), "A", 1, 100000f, BookingStatus.Reserved, LocalDateTime.of(2025, 9, 18, 17, 48)), // User 1, GODZILLA VS KONG, CGV Vincom
            createBooking(users.get(1), shows.get(1), "B", 5, 120000f, BookingStatus.Confirmed, LocalDateTime.of(2025, 9, 17, 17, 48)), // User 2, JOKER, Lotte Cinema
            createBooking(users.get(2), shows.get(2), "C", 10, 150000f, BookingStatus.Confirmed, LocalDateTime.of(2025, 9, 18, 15, 48)), // User 3, THE BATMAN, BHD Star
            createBooking(users.get(3), shows.get(3), "D", 3, 90000f, BookingStatus.Cancelled, LocalDateTime.of(2025, 9, 16, 17, 48)), // User 4, DEADPOOL, Galaxy Nguyễn Trãi
            createBooking(users.get(4), shows.get(4), "E", 7, 110000f, BookingStatus.Reserved, LocalDateTime.of(2025, 9, 18, 17, 48)) // User 5, AVENGERS: ENDGAME, Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        bookingRepository.saveAll(bookings);
        System.out.println("Seeded " + bookings.size() + " bookings");
    }
    
    private Booking createBooking(User user, Show show, String seatRow, Integer seatNumber, Float price, BookingStatus status, LocalDateTime bookingDateTime) {
        if (user == null || show == null) {
            return null; // Bỏ qua vé đặt chỗ không hợp lệ
        }
        Booking booking = new Booking();
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
            createPayment(100000, LocalDateTime.of(2025, 9, 18, 17, 53), PaymentMethod.Card, users.get(0), shows.get(0)), // User 1, GODZILLA VS KONG, CGV Vincom
            createPayment(120000, LocalDateTime.of(2025, 9, 18, 16, 53), PaymentMethod.Card, users.get(1), shows.get(1)), // User 2, JOKER, Lotte Cinema
            createPayment(150000, LocalDateTime.of(2025, 9, 18, 15, 53), PaymentMethod.Card, users.get(2), shows.get(2)), // User 3, THE BATMAN, BHD Star
            createPayment(90000, LocalDateTime.of(2025, 9, 18, 14, 53), PaymentMethod.Cash, users.get(3), shows.get(3)), // User 4, DEADPOOL, Galaxy Nguyễn Trãi
            createPayment(110000, LocalDateTime.of(2025, 9, 18, 13, 53), PaymentMethod.Cash, users.get(4), shows.get(4)) // User 5, AVENGERS: ENDGAME, Quốc Gia
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
        paymentRepository.saveAll(payments);
        System.out.println("Seeded " + payments.size() + " payments");
    }
    
    private Payment createPayment(Integer amount, LocalDateTime paymentDateTime, PaymentMethod method, User user, Show show) {
        if (user == null || show == null) {
            return null; // Bỏ qua thanh toán không hợp lệ
        }
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentDateTime(paymentDateTime);
        payment.setMethod(method);
        payment.setUser(user);
        payment.setShow(show);
        return payment;
    }
}