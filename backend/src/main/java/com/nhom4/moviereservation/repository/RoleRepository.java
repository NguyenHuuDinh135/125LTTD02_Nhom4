package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần
}