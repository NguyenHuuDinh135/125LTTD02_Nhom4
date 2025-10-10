package com.nhom4.moviereservation.repository;

import com.nhom4.moviereservation.model.MovieRole;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.MovieRole;

@Repository
public interface MovieRoleRepository extends JpaRepository<MovieRole, Long> {
    List<MovieRole> findByMovieId(Integer id);
    List<MovieRole> findByRoleId(Integer roleId);

}