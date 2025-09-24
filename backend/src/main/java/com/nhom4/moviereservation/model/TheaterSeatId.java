package com.nhom4.moviereservation.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterSeatId implements Serializable {
    private String seatRow;
    private Integer seatNumber;
    private Integer theaterId;
}