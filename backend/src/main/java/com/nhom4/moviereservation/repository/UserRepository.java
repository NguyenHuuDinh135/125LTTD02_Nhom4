package com.nhom4.moviereservation.repository;

import com.nhom4.moviereservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Có thể thêm custom query nếu cần, ví dụ: findByEmail
}