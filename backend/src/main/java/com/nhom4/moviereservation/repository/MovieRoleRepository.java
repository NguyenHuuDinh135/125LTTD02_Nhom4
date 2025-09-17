package com.nhom4.moviereservation.repository;

import com.nhom4.moviereservation.model.MovieRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRoleRepository extends JpaRepository<MovieRole, Long> {
}