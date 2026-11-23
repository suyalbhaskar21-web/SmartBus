package com.smartbus.repository;

import com.smartbus.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    Optional<Booking> findByPnrIgnoreCase(String pnr);
    boolean existsByBusIdAndTravelDateAndSeatNumberAndStatus(Long busId, String travelDate, int seatNumber, String status);
}
