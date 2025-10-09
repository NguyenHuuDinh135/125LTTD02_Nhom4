package com.nhom4.moviereservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.MovieRole;

@Repository
public interface MovieRoleRepository extends JpaRepository<MovieRole, Long> {
    List<MovieRole> findByRoleId(Integer roleId);
}