package com.nhom4.moviereservation.repository;

import com.nhom4.moviereservation.model.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}