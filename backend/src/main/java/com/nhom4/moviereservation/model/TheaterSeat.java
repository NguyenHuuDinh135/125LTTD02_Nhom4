package com.nhom4.moviereservation.model;

import java.io.Serializable;
import com.nhom4.moviereservation.model.enums.SeatType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TheaterSeatId implements Serializable {
        private String seatRow;
        private Integer seatNumber;
        private Long theaterId; // phải có vì seatRow + seatNumber không đủ duy nhất
    }

    @EmbeddedId
    private TheaterSeatId id;

    @ManyToOne
    @MapsId("theaterId")
    @JoinColumn(name = "theater_id")
    private Theater theater;

    @Enumerated(EnumType.STRING)
    private SeatType type;
}