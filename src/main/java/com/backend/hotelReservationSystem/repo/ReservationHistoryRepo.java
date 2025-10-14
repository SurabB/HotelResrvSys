package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.ReservationHistory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ReservationHistoryRepo extends JpaRepository<ReservationHistory,Long> {

}
