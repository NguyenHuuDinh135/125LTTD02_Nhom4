package com.nhom4.moviereservation.model;

import com.nhom4.moviereservation.model.enums.SeatType;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theater_seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterSeat {
    @EmbeddedId
    private TheaterSeatId id;

    @ManyToOne
    @MapsId("theaterId")
    @JoinColumn(name = "theater_id", foreignKey = @ForeignKey(name = "fk_theater_seats_theater_id"))
    private Theater theater;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SeatType type;
}