package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ: findByMovieIdAndDate(Long movieId, LocalDate date);
}