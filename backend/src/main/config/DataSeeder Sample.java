// package com.masai.config;

// import java.time.LocalDate;
// import java.util.Arrays;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import com.masai.model.Bus;
// import com.masai.model.Reservation;
// import com.masai.model.Route;
// import com.masai.model.User;
// import com.masai.repository.BusRepository;
// import com.masai.repository.ReservationRepository;
// import com.masai.repository.RouteRepository;
// import com.masai.repository.UserRepository;

// @Component
// public class DataSeeder implements CommandLineRunner {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private RouteRepository routeRepository;

//     @Autowired
//     private BusRepository busRepository;

//     @Autowired
//     private ReservationRepository reservationRepository;

//     @Override
//     public void run(String... args) throws Exception {
//         if (userRepository.count() == 0) {
//             seedUsers();
//             seedRoutes();
//             seedBuses();
//             seedReservations();
//             System.out.println("Database seeded successfully!");
//         } else {
//             System.out.println("Database already contains data. Skipping seeding.");
//         }
//     }

//     private void seedUsers() {
//         List<User> users = Arrays.asList(
//             createUser("Nguyễn Văn A", "user1@example.com", "password123"),
//             createUser("Trần Thị B", "user2@example.com", "password123"),
//             createUser("Lê Văn C", "user3@example.com", "password123"),
//             createUser("Phạm Thị D", "user4@example.com", "password123"),
//             createUser("Hoàng Văn E", "user5@example.com", "password123"),
//             createUser("Ngô Thị F", "user6@example.com", "password123"),
//             createUser("Đặng Văn G", "user7@example.com", "password123"),
//             createUser("Vũ Thị H", "user8@example.com", "password123"),
//             createUser("Bùi Văn I", "user9@example.com", "password123"),
//             createUser("Đỗ Thị J", "user10@example.com", "password123")
//         );
//         userRepository.saveAll(users);
//         System.out.println("Seeded " + users.size() + " users");
//     }

//     private User createUser(String name, String email, String password) {
//         User user = new User();
//         user.setName(name);
//         user.setEmail(email);
//         user.setPassword(password);
//         return user;
//     }

//     private void seedRoutes() {
//         List<Route> routes = Arrays.asList(
//             createRoute("Hà Nội", "Hải Phòng", 100, 50.0),
//             createRoute("Hà Nội", "Đà Nẵng", 700, 300.0),
//             createRoute("Hà Nội", "Sài Gòn", 1700, 800.0),
//             createRoute("Hà Nội", "Huế", 600, 250.0),
//             createRoute("Hà Nội", "Nha Trang", 1300, 600.0),
//             createRoute("Hà Nội", "Cần Thơ", 1900, 900.0),
//             createRoute("Hà Nội", "Quảng Ninh", 150, 70.0),
//             createRoute("Hà Nội", "Lào Cai", 300, 150.0),
//             createRoute("Hà Nội", "Điện Biên", 500, 200.0),
//             createRoute("Hà Nội", "Thanh Hóa", 150, 80.0)
//         );
//         routeRepository.saveAll(routes);
//         System.out.println("Seeded " + routes.size() + " routes");
//     }

//     private Route createRoute(String from, String to, int distance, double price) {
//         Route route = new Route();
//         route.setRouteFrom(from);
//         route.setRouteTo(to);
//         route.setDistance(distance);
//         route.setPrice(price);
//         return route;
//     }

//     private void seedBuses() {
//         List<Route> routes = routeRepository.findAll();
        
//         List<Bus> buses = Arrays.asList(
//             createBus("Xe buýt 1", "Tài xế A", "Luxury", "09:00", "07:00", 40, 30, routes.get(0)), // Hà Nội → Hải Phòng
//             createBus("Xe buýt 2", "Tài xế B", "Standard", "09:00", "08:00", 30, 20, routes.get(0)), // Hà Nội → Hải Phòng
//             createBus("Xe buýt 3", "Tài xế C", "Luxury", "12:00", "09:00", 50, 40, routes.get(1)), // Hà Nội → Đà Nẵng
//             createBus("Xe buýt 4", "Tài xế D", "Standard", "12:00", "10:00", 35, 25, routes.get(1)), // Hà Nội → Đà Nẵng
//             createBus("Xe buýt 5", "Tài xế E", "Luxury", "15:00", "11:00", 45, 35, routes.get(2)), // Hà Nội → Sài Gòn
//             createBus("Xe buýt 6", "Tài xế F", "Standard", "15:00", "12:00", 25, 15, routes.get(2)), // Hà Nội → Sài Gòn
//             createBus("Xe buýt 7", "Tài xế G", "Luxury", "11:30", "13:00", 55, 45, routes.get(3)), // Hà Nội → Huế
//             createBus("Xe buýt 8", "Tài xế H", "Standard", "11:30", "14:00", 20, 10, routes.get(3)), // Hà Nội → Huế
//             createBus("Xe buýt 9", "Tài xế I", "Luxury", "13:00", "15:00", 60, 50, routes.get(4)), // Hà Nội → Nha Trang
//             createBus("Xe buýt 10", "Tài xế J", "Standard", "13:00", "16:00", 30, 20, routes.get(4)), // Hà Nội → Nha Trang
//             createBus("Xe buýt 11", "Tài xế K", "Luxury", "14:00", "17:00", 40, 30, routes.get(5)), // Hà Nội → Cần Thơ
//             createBus("Xe buýt 12", "Tài xế L", "Standard", "14:00", "18:00", 35, 25, routes.get(5)), // Hà Nội → Cần Thơ
//             createBus("Xe buýt 13", "Tài xế M", "Luxury", "08:00", "19:00", 50, 40, routes.get(6)), // Hà Nội → Quảng Ninh
//             createBus("Xe buýt 14", "Tài xế N", "Standard", "08:00", "20:00", 30, 20, routes.get(6)), // Hà Nội → Quảng Ninh
//             createBus("Xe buýt 15", "Tài xế O", "Luxury", "22:00", "21:00", 45, 35, routes.get(7)), // Hà Nội → Lào Cai
//             createBus("Xe buýt 16", "Tài xế P", "Standard", "22:00", "22:00", 25, 15, routes.get(7)), // Hà Nội → Lào Cai
//             createBus("Xe buýt 17", "Tài xế Q", "Luxury", "06:00", "07:00", 55, 45, routes.get(8)), // Hà Nội → Điện Biên
//             createBus("Xe buýt 18", "Tài xế R", "Standard", "06:00", "08:00", 20, 10, routes.get(8)), // Hà Nội → Điện Biên
//             createBus("Xe buýt 19", "Tài xế S", "Luxury", "10:00", "09:00", 60, 50, routes.get(9)), // Hà Nội → Thanh Hóa
//             createBus("Xe buýt 20", "Tài xế T", "Standard", "10:00", "10:00", 30, 20, routes.get(9))  // Hà Nội → Thanh Hóa
//         );
//         busRepository.saveAll(buses);
//         System.out.println("Seeded " + buses.size() + " buses");
//     }

//     private Bus createBus(String busName, String driverName, String busType, 
//                          String arrivalTime, String departureTime, int seats, 
//                          int availableSeats, Route route) {
//         Bus bus = new Bus();
//         bus.setBusName(busName);
//         bus.setDriverName(driverName);
//         bus.setBusType(busType);
//         bus.setArrivalTime(arrivalTime);
//         bus.setDepartureTime(departureTime);
//         bus.setSeats(seats);
//         bus.setAvailableSeats(availableSeats);
//         bus.setRoute(route);
//         return bus;
//     }

//     private void seedReservations() {
//         List<User> users = userRepository.findAll();
//         List<Bus> buses = busRepository.findAll();
        
//         List<Reservation> reservations = Arrays.asList(
//             // Đặt vé cho Nguyễn Văn A (user 0)
//             createReservation("Xác Nhận", LocalDate.now().plusDays(1), "07:30", 2, buses.get(0), users.get(0)), // Hà Nội → Hải Phòng
//             createReservation("Xác Nhận", LocalDate.now().plusDays(2), "08:00", 1, buses.get(2), users.get(0)), // Hà Nội → Đà Nẵng
//             createReservation("Xác Nhận", LocalDate.now().plusDays(1), "09:00", 3, buses.get(4), users.get(0)), // Hà Nội → Sài Gòn
//             createReservation("Xác Nhận", LocalDate.now().plusDays(3), "10:30", 2, buses.get(6), users.get(0)), // Hà Nội → Huế
//             // Đặt vé cho Trần Thị B (user 1)
//             createReservation("Xác Nhận", LocalDate.now().plusDays(1), "08:30", 1, buses.get(1), users.get(1)), // Hà Nội → Hải Phòng
//             createReservation("Xác Nhận", LocalDate.now().plusDays(2), "09:00", 2, buses.get(3), users.get(1)), // Hà Nội → Đà Nẵng
//             createReservation("Xác Nhận", LocalDate.now().plusDays(1), "10:00", 1, buses.get(5), users.get(1)), // Hà Nội → Sài Gòn
//             createReservation("Xác Nhận", LocalDate.now().plusDays(4), "11:00", 3, buses.get(7), users.get(1)), // Hà Nội → Huế
//             // Đặt vé cho các user khác
//             createReservation("Xác Nhận", LocalDate.now().plusDays(1), "15:30", 3, buses.get(8), users.get(8)), // Hà Nội → Nha Trang
//             createReservation("Hủy", LocalDate.now().plusDays(1), "16:30", 1, buses.get(9), users.get(9)) // Hà Nội → Nha Trang
//         );
//         reservationRepository.saveAll(reservations);
//         System.out.println("Seeded " + reservations.size() + " reservations");
//     }

//     private Reservation createReservation(String status, LocalDate date, String time, int numberOfTickets, Bus bus, User user) {
//         Reservation reservation = new Reservation();
//         reservation.setReservationStatus(status);
//         reservation.setReservationDate(date);
//         reservation.setReservationTime(time);
//         reservation.setNumberOfTickets(numberOfTickets);
//         reservation.setBus(bus);
//         reservation.setUser(user);
//         return reservation;
//     }
// }