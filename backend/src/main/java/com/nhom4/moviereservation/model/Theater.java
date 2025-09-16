package com.nhom4.moviereservation.model;

import java.util.List;

import com.nhom4.moviereservation.model.enums.TheaterType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theaters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theater{
    @Id
    private Long id;
    private String name;
    private Integer numOfRows;
    private Integer seatsPerRow;

    @Enumerated(EnumType.STRING)
    private TheaterType type;

    @OneToMany(mappedBy = "theater")
    private List<Show> shows;

    @OneToMany(mappedBy = "theater")
    private List<TheaterSeat> theaterSeats;

    // Getters and setters
}