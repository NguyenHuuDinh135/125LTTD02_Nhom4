package com.nhom4.moviereservation.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.nhom4.moviereservation.model.enums.ShowStatus;
import com.nhom4.moviereservation.model.enums.ShowType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shows",
       indexes = {
           @Index(name = "fk_shows_movie_id", columnList = "movie_id"),
           @Index(name = "fk_shows_theater_id", columnList = "theater_id")
       },
       uniqueConstraints = @UniqueConstraint(name = "unique_show_details", columnNames = {"start_time", "end_time", "date", "movie_id", "theater_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Integer id;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "movie_id", foreignKey = @ForeignKey(name = "fk_shows_movie_id"))
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "theater_id", foreignKey = @ForeignKey(name = "fk_shows_theater_id"))
    private Theater theater;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_status")
    private ShowStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_type")
    private ShowType type;

    @OneToMany(mappedBy = "show")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "show")
    private List<Payment> payments;

    // Getters and setters
}