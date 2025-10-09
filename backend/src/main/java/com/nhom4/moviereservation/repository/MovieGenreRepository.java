package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.MovieGenre;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
}