package com.nhom4.moviereservation.model;

import java.time.LocalDateTime;

import com.nhom4.moviereservation.model.enums.BookingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings" ,
        indexes = @Index(name = "fk_bookings_show_id", columnList = "show_id"),
        uniqueConstraints = @UniqueConstraint(name = "unique_booking_details", columnNames = {"user_id", "show_id", "seat_row", "seat_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_bookings_user_id"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "price")
    private Float price;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus status;

    @Column(name = "booking_date_time")
    private LocalDateTime bookingDateTime;
    

    // Getters and setters
}