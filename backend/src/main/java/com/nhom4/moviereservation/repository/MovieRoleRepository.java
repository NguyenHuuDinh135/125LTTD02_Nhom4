package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.MovieRole;

@Repository
public interface MovieRoleRepository extends JpaRepository<MovieRole, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ: findByMovieId(Long movieId);
}