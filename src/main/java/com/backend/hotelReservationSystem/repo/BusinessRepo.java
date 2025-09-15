package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepo extends JpaRepository<Business,Long> {
}
