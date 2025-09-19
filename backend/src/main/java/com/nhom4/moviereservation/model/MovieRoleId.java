package com.nhom4.moviereservation.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRoleId implements Serializable {
    private Long movieId;
    private Long roleId;
}