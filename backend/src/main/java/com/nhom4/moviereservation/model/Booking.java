package com.nhom4.moviereservation.model;

import com.nhom4.moviereservation.model.enums.BookingStatus;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    private String seatRow;
    private Integer seatNumber;
    private Float price;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime bookingDateTime;

    // Getters and setters
}