// package com.nhom4.moviereservation.config;

// import java.time.LocalDate;
// import java.util.Arrays;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Primary;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import com.nhom4.moviereservation.model.*;

// import com.nhom4.moviereservation.repository.*;

// // import jakarta.persistence.EntityManager;
// // import jakarta.persistence.PersistenceContext;


// @Component
// public class DataSeeder implements CommandLineRunner {

//     @Autowired
//     private BookingRepository bookingRepository;

//     @Autowired
//     private GenreRepository genreRepository;

//     @Autowired
//     private MovieRepository movieRepository;

//     @Autowired
//     private MovieGenreRepository movieGenreRepository;

//     @Autowired
//     private MovieRoleRepository movieRoleRepository;

//     @Autowired
//     private PaymentRepository paymentRepository;

//     @Autowired
//     private RoleRepository roleRepository;

//     @Autowired
//     private ShowRepository showRepository;

//     @Autowired
//     private TheaterRepository theaterRepository;

//     @Autowired
//     private TheaterSeatRepository theaterSeatRepository;

//     @Autowired
//     private UserRepository userRepository;


//     @Override
//     public void run(String... args) throws Exception {
//         if (userRepository.count() == 0) {
//             seedUsers();
//             seedBookings();
//             seedPayments();
//             seedGenres();
//             seedMovies();
//             seedMovieGenres();
//             seedRoles();
//             seedMovieRoles();
//             seedTheaters();
//             seedTheaterSeats();
//             seedShows();
//             System.out.println("Database seeded successfully!");
//         } else {
//             System.out.println("Database already contains data. Skipping seeding.");
//         }
//     }
//     private void seedUsers() {
//         List<User> users = Arrays.asList(
//             createUser(1, "Nguyễn Văn A", "user1@example.com", "password123", "Hà Nội", "0912345678", "admin"),
//             createUser(2, "Trần Thị B", "user2@example.com", "password123", "Hồ Chí Minh", "0987654321", "api_user"),
//             createUser(3, "Lê Văn C", "user3@example.com", "password123", "Đà Nẵng", "0911112222", "api_user"),
//             createUser(4, "Phạm Thị D", "user4@example.com", "password123", "Cần Thơ", "0903334444", "api_user"),
//             createUser(5, "Hoàng Văn E", "user5@example.com", "password123", "Hải Phòng", "0935556666", "api_user"),
//             createUser(6, "Ngô Thị F", "user6@example.com", "password123", "Huế", "0927778888", "api_user"),
//             createUser(7, "Đặng Văn G", "user7@example.com", "password123", "Nha Trang", "0919990000", "api_user"),
//             createUser(8, "Vũ Thị H", "user8@example.com", "password123", "Vũng Tàu", "0988889999", "api_user"),
//             createUser(9, "Bùi Văn I", "user9@example.com", "password123", "Quảng Ninh", "0912223333", "api_user"),
//             createUser(10, "Đỗ Thị J", "user10@example.com", "password123", "Đà Lạt", "0904445555", "api_user")
//         );
//         userRepository.saveAll(users);
//         System.out.println("Seeded " + users.size() + " users");
//     }

//     private User createUser(int dummyId, String fullName, String email, String password, String address, String contact, String role) {
//         User user = new User();
//         user.setId((long) dummyId); // Chuyển int sang Long và gán vào id
//         user.setFullName(fullName);
//         user.setEmail(email);
//         user.setPassword(password);
//         user.setAddress(address);
//         user.setContact(contact);
//         user.setRole(role);
//         return user;
//     }

//     private void seedGenres() {
//         List<Genre> genres = Arrays.asList(
//             createGenre(1L, "Action"),
//             createGenre(2L, "Drama"),
//             createGenre(3L, "Comedy")
//         );
//         genreRepository.saveAll(genres);
//         System.out.println("Seeded " + genres.size() + " genres");
//     }
    
//     private Genre createGenre(Long id, String name) {
//         Genre genre = new Genre();
//         genre.setId(id);
//         genre.setName(name);
//         return genre;
//     }

//     private void seedMovies() {
//         List<Movie> movies = Arrays.asList(
//             createMovie(1L, "GODZILLA VS KONG", "Legends collide as Godzilla and Kong", 2019, new BigDecimal("8.4"), "https://trailer.com/avengers", "https://poster.com/avengers.jpg", MovieType.ACTION),
//             createMovie(2L, "JOKER", "Arthur Fleck, a party clown, leads an impoverished life with his ailing mother. However, when society shuns him and brands him as a freak, he decides to embrace the life of crime and chaos.", 1972, new BigDecimal("9.2"), "https://trailer.com/godfather", "https://poster.com/godfather.jpg", MovieType.DRAMA),
//             createMovie(3L, "THE BATMAN", "The Riddler plays a deadly game of cat and mouse with Batman and Commissioner Gordon in Gotham City.", 2016, new BigDecimal("8.0"), "https://trailer.com/deadpool", "https://poster.com/deadpool.jpg", MovieType.COMEDY)
//         );
//         movieRepository.saveAll(movies);
//         System.out.println("Seeded \"" + movies.size() + " movies");
//     }
    
//     private void seedRoles() {
//         List<Role> roles = Arrays.asList(
//             createRole(1L, "Robert Downey Jr.", (byte) 60, "https://example.com/rdj.jpg"),
//             createRole(2L, "Christopher Nolan", (byte) 55, "https://example.com/nolan.jpg"),
//             createRole(3L, "Scarlett Johansson", (byte) 40, "https://example.com/scarlett.jpg"),
//             createRole(4L, "James Cameron", (byte) 70, "https://example.com/cameron.jpg"),
//             createRole(5L, "Ryan Reynolds", (byte) 48, "https://example.com/ryan.jpg"),
//             createRole(6L, "Tom Hanks", (byte) 68, "https://example.com/tom.jpg"),
//             createRole(7L, "Pixar Team", (byte) 30, "https://example.com/pixar.jpg"),
//             createRole(8L, "Morgan Freeman", (byte) 87, "https://example.com/morgan.jpg"),
//             createRole(9L, "Martin Scorsese", (byte) 82, "https://example.com/scorsese.jpg"),
//             createRole(10L, "Leonardo DiCaprio", (byte) 50, "https://example.com/leo.jpg")
//         );
//         roleRepository.saveAll(roles);
//         System.out.println("Seeded " + roles.size() + " roles");
//     }
    
//     private Role createRole(Long id, String fullName, Byte age, String pictureUrl) {
//         Role role = new Role();
//         role.setId(id);
//         role.setFullName(fullName);
//         role.setAge(age);
//         role.setPictureUrl(pictureUrl);
//         return role;
//     }

//     private Movie createMovie(Long id, String title, String summary, Integer year, BigDecimal rating, String trailerUrl, String posterUrl, MovieType movieType) {
//         Movie movie = new Movie();
//         movie.setId(id);
//         movie.setTitle(title);
//         movie.setSummary(summary);
//         movie.setYear(year);
//         movie.setRating(rating);
//         movie.setTrailerUrl(trailerUrl);
//         movie.setPosterUrl(posterUrl);
//         movie.setMovieType(movieType);
//         return movie;
//     }

//     private void seedMovieGenres() {
//         Movie movie1 = movieRepository.findById(1L).orElse(null);
//         Movie movie2 = movieRepository.findById(2L).orElse(null);
//         Movie movie3 = movieRepository.findById(3L).orElse(null);
//         Genre genre1 = genreRepository.findById(1L).orElse(null);
//         Genre genre2 = genreRepository.findById(2L).orElse(null);
//         Genre genre3 = genreRepository.findById(3L).orElse(null);
    
//         List<MovieGenre> movieGenres = Arrays.asList(
//             createMovieGenre(movie1, genre1),
//             createMovieGenre(movie2, genre2),
//             createMovieGenre(movie3, genre3)
//         );
//         movieGenreRepository.saveAll(movieGenres);
//         System.out.println("Seeded" + movieGenres.size() + " movieGenres");
//     }
    
//     private MovieGenre createMovieGenre(Movie movie, Genre genre) {
//         MovieGenre movieGenre = new MovieGenre();
//         movieGenre.setMovie(movie);
//         movieGenre.setGenre(genre);
//         return movieGenre;
//     }

//     private void seedMovieRoles() {
//         Movie movie1 = movieRepository.findById(1L).orElse(null);
//         Movie movie2 = movieRepository.findById(2L).orElse(null);
//         Movie movie3 = movieRepository.findById(3L).orElse(null);
//         Role role1 = roleRepository.findById(1L).orElse(null);
//         Role role2 = roleRepository.findById(2L).orElse(null);
//         Role role3 = roleRepository.findById(3L).orElse(null);
    
//         List<MovieRole> movieRoles = Arrays.asList(
//             createMovieRole(movie1, role1, RoleType.DIRECTOR),
//             createMovieRole(movie2, role2, RoleType.ACTOR),
//             createMovieRole(movie3, role3, RoleType.PRODUCER)
//         );
//         movieRoleRepository.saveAll(movieRoles);
//         System.out.println("Seeded " + movieRoles.size() + " movieRoles");
//     }
    
//     private MovieRole createMovieRole(Movie movie, Role role, RoleType roleType) {
//         MovieRole movieRole = new MovieRole();
//         movieRole.setMovie(movie);
//         movieRole.setRole(role);
//         movieRole.setRoleType(roleType);
//         return movieRole;
//     }

//     private void seedBookings() {
//         List<User> users = userRepository.findAll();
//         List<Show> shows = showRepository.findAll();
    
//         List<Booking> bookings = Arrays.asList(
//             createBooking(1L, users.get(0), shows.get(0), "A", 1, 100000f, BookingStatus.PENDING, LocalDateTime.now()),
//             createBooking(2L, users.get(1), shows.get(1), "B", 5, 120000f, BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(1)),
//             createBooking(3L, users.get(2), shows.get(2), "C", 10, 150000f, BookingStatus.CONFIRMED, LocalDateTime.now().minusHours(2)),
//             createBooking(4L, users.get(3), shows.get(3), "D", 3, 90000f, BookingStatus.CANCELLED, LocalDateTime.now().minusDays(2)),
//             createBooking(5L, users.get(4), shows.get(4), "E", 7, 110000f, BookingStatus.PENDING, LocalDateTime.now())
//         );
//         bookingRepository.saveAll(bookings);
//         System.out.println("Seeded " + bookings.size() + " bookings");
//     }
    
//     private Booking createBooking(Long id, User user, Show show, String seatRow, Integer seatNumber, Float price, BookingStatus status, LocalDateTime bookingDateTime) {
//         Booking booking = new Booking();
//         booking.setId(id);
//         booking.setUser(user);
//         booking.setShow(show);
//         booking.setSeatRow(seatRow);
//         booking.setSeatNumber(seatNumber);
//         booking.setPrice(price);
//         booking.setStatus(status);
//         booking.setBookingDateTime(bookingDateTime);
//         return booking;
//     }

//     private void seedPayments() {
//         List<User> users = userRepository.findAll();
//         List<Show> shows = showRepository.findAll();
    
//         if (users.isEmpty() || shows.isEmpty()) {
//             System.out.println("Không thể khởi tạo Payment vì thiếu User hoặc Show");
//             return;
//         }
    
//         List<Payment> payments = Arrays.asList(
//             createPayment(1L, 100000, LocalDateTime.now(), PaymentMethod.CREDIT_CARD, users.get(0), shows.get(0)),
//             createPayment(2L, 120000, LocalDateTime.now().minusHours(1), PaymentMethod.CASH, users.get(1), shows.get(1)),
//             createPayment(3L, 150000, LocalDateTime.now().minusHours(2), PaymentMethod.MOBILE_PAYMENT, users.get(2), shows.get(2)),
//             createPayment(4L, 130000, LocalDateTime.now().minusHours(3), PaymentMethod.CREDIT_CARD, users.get(3), shows.get(3)),
//             createPayment(5L, 110000, LocalDateTime.now().minusHours(4), PaymentMethod.CASH, users.get(4), shows.get(4)),
//             createPayment(6L, 140000, LocalDateTime.now().minusHours(5), PaymentMethod.MOBILE_PAYMENT, users.get(5), shows.get(5)),
//             createPayment(7L, 160000, LocalDateTime.now().minusHours(6), PaymentMethod.CREDIT_CARD, users.get(6), shows.get(6)),
//             createPayment(8L, 170000, LocalDateTime.now().minusHours(7), PaymentMethod.CASH, users.get(7), shows.get(7)),
//             createPayment(9L, 180000, LocalDateTime.now().minusHours(8), PaymentMethod.MOBILE_PAYMENT, users.get(8), shows.get(8)),
//             createPayment(10L, 190000, LocalDateTime.now().minusHours(9), PaymentMethod.CREDIT_CARD, users.get(9), shows.get(9))
//         );
//         paymentRepository.saveAll(payments);
//         System.out.println("Seeded " + payments.size() + " payments");
//     }
    
//     private Payment createPayment(Long id, Integer amount, LocalDateTime paymentDateTime, PaymentMethod method, User user, Show show) {
//         Payment payment = new Payment();
//         payment.setId(id);
//         payment.setAmount(amount);
//         payment.setPaymentDateTime(paymentDateTime);
//         payment.setMethod(method);
//         payment.setUser(user);
//         payment.setShow(show);
//         return payment;
//     }

//     private void seedTheaters() {
//         List<Theater> theaters = Arrays.asList(
//             createTheater(1L, "Rạp CGV Vincom", 10, 20, TheaterType.STANDARD),
//             createTheater(2L, "Rạp Lotte Cinema", 8, 15, TheaterType.VIP),
//             createTheater(3L, "Rạp BHD Star", 12, 18, TheaterType.STANDARD),
//             createTheater(4L, "Rạp Galaxy Nguyễn Trãi", 10, 22, TheaterType.IMAX),
//             createTheater(5L, "Rạp Quốc Gia", 6, 10, TheaterType.STANDARD)
//         );
//         theaterRepository.saveAll(theaters);
//         System.out.println("Seeder " + theaters.size() + " theaters");
//     }
    
//     private Theater createTheater(Long id, String name, Integer numOfRows, Integer seatsPerRow, TheaterType type) {
//         Theater theater = new Theater();
//         theater.setId(id);
//         theater.setName(name);
//         theater.setNumOfRows(numOfRows);
//         theater.setSeatsPerRow(seatsPerRow);
//         theater.setType(type);
//         return theater;
//     }

//     private void seedTheaterSeats() {
//         List<Theater> theaters = theaterRepository.findAll();
    
//         if (theaters.isEmpty()) {
//             System.out.println("Không thể khởi tạo TheaterSeat vì thiếu Theater");
//             return;
//         }
    
//         List<TheaterSeat> theaterSeats = Arrays.asList(
//             createTheaterSeat("A", 1, theaters.get(0), SeatType.STANDARD),
//             createTheaterSeat("A", 2, theaters.get(0), SeatType.STANDARD),
//             createTheaterSeat("B", 1, theaters.get(1), SeatType.VIP),
//             createTheaterSeat("B", 2, theaters.get(1), SeatType.VIP),
//             createTheaterSeat("C", 1, theaters.get(2), SeatType.STANDARD),
//             createTheaterSeat("C", 2, theaters.get(2), SeatType.STANDARD),
//             createTheaterSeat("D", 1, theaters.get(3), SeatType.IMAX),
//             createTheaterSeat("D", 2, theaters.get(3), SeatType.IMAX),
//             createTheaterSeat("E", 1, theaters.get(4), SeatType.STANDARD),
//             createTheaterSeat("E", 2, theaters.get(4), SeatType.STANDARD)
//         );
//         theaterSeatRepository.saveAll(theaterSeats);
//         System.out.println("Seeded " + theaterSeats.size() + " theaterSeats");
//     }
    
//     private TheaterSeat createTheaterSeat(String seatRow, Integer seatNumber, Theater theater, SeatType type) {
//         TheaterSeat theaterSeat = new TheaterSeat();
//         theaterSeat.setSeatRow(seatRow);
//         theaterSeat.setSeatNumber(seatNumber);
//         theaterSeat.setTheater(theater);
//         theaterSeat.setType(type);
//         return theaterSeat;
//     }

//     private void seedShows() {
//         List<Movie> movies = movieRepository.findAll();
//         List<Theater> theaters = theaterRepository.findAll();
    
//         if (movies.isEmpty() || theaters.isEmpty()) {
//             System.out.println("Không thể khởi tạo Show vì thiếu Movie hoặc Theater");
//             return;
//         }
    
//         List<Show> shows = Arrays.asList(
//             createShow(1L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 1), movies.get(0), theaters.get(0), ShowStatus.SCHEDULED, ShowType.TWO_D),
//             createShow(2L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 1), movies.get(1), theaters.get(1), ShowStatus.SCHEDULED, ShowType.THREE_D),
//             createShow(3L, LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 1), movies.get(2), theaters.get(2), ShowStatus.SCHEDULED, ShowType.TWO_D),
//             createShow(4L, LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 1), movies.get(3), theaters.get(3), ShowStatus.SCHEDULED, ShowType.IMAX),
//             createShow(5L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 2), movies.get(4), theaters.get(4), ShowStatus.SCHEDULED, ShowType.TWO_D),
//             createShow(6L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 2), movies.get(5), theaters.get(0), ShowStatus.SCHEDULED, ShowType.THREE_D),
//             createShow(7L, LocalTime.of(16, 0), LocalTime.of(18, 30), LocalDate.of(2025, 10, 2), movies.get(6), theaters.get(1), ShowStatus.SCHEDULED, ShowType.TWO_D),
//             createShow(8L, LocalTime.of(19, 0), LocalTime.of(21, 30), LocalDate.of(2025, 10, 2), movies.get(7), theaters.get(2), ShowStatus.SCHEDULED, ShowType.IMAX),
//             createShow(9L, LocalTime.of(10, 0), LocalTime.of(12, 30), LocalDate.of(2025, 10, 3), movies.get(8), theaters.get(3), ShowStatus.SCHEDULED, ShowType.TWO_D),
//             createShow(10L, LocalTime.of(13, 0), LocalTime.of(15, 30), LocalDate.of(2025, 10, 3), movies.get(9), theaters.get(4), ShowStatus.SCHEDULED, ShowType.THREE_D)
//         );
//         showRepository.saveAll(shows);
//         System.out.println("Seeded " + shows.size() + " shows");
//     }
    
//     private Show createShow(Long id, LocalTime startTime, LocalTime endTime, LocalDate date, Movie movie, Theater theater, ShowStatus status, ShowType type) {
//         Show show = new Show();
//         show.setId(id);
//         show.setStartTime(startTime);
//         show.setEndTime(endTime);
//         show.setDate(date);
//         show.setMovie(movie);
//         show.setTheater(theater);
//         show.setStatus(status);
//         show.setType(type);
//         return show;
//     }
//     --commit
// }