package com.nhom4.moviereservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.MovieGenre;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
    // Thêm phương thức để tìm danh sách MovieGenre theo genreId
    List<MovieGenre> findByGenreId(Integer genreId);
}