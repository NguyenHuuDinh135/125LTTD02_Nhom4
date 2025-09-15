package com.nhom4.moviereservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.nhom4.moviereservation.model.enums.ShowStatus;
import com.nhom4.moviereservation.model.enums.ShowType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shows")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Show {
    @Id
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "theater_id")
    private Theater theater;

    @Enumerated(EnumType.STRING)
    private ShowStatus status;

    @Enumerated(EnumType.STRING)
    private ShowType type;

    @OneToMany(mappedBy = "show")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "show")
    private List<Payment> payments;

    // Getters and setters
}