package com.nhom4.moviereservation.model;

import java.time.LocalDateTime;

import com.nhom4.moviereservation.model.enums.BookingStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private Long id;

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