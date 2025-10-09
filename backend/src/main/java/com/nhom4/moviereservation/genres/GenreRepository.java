package com.nhom4.moviereservation.genres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
}