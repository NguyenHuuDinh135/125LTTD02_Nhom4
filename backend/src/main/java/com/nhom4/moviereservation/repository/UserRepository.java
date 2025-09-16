package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ: findByEmail(String email);
}