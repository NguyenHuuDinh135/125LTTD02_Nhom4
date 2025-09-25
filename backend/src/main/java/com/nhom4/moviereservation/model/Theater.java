package com.nhom4.moviereservation.model;

import java.util.List;

import com.nhom4.moviereservation.model.enums.TheaterType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theaters",
       uniqueConstraints = @UniqueConstraint(name = "theater_name", columnNames = "theater_name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theater{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theater_id")
    private Integer id;
    @Column(name = "theater_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TheaterType type;

    @OneToMany(mappedBy = "theater")
    private List<Show> shows;

    @OneToMany(mappedBy = "theater")
    private List<TheaterSeat> theaterSeats;

    // Getters and setters
}