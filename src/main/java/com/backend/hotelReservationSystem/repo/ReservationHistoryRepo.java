package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationHistoryRepo extends JpaRepository<ReservationHistory,Long> {

}
