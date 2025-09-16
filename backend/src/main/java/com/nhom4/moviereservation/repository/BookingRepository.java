package com.nhom4.moviereservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom4.moviereservation.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần
}