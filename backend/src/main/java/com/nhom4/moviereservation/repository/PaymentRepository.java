package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần
}