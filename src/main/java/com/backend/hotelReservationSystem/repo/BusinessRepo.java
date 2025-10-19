package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.Business;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface BusinessRepo extends JpaRepository<Business,Long> {
    @Query("select b from Business b join b.user u where b.businessName=:businessName and b.cityName=:city and b.location=:location and u.isActive=true")
    Optional<Business> findBusiness(String businessName, String city, String location);

    @Query("select b from Business b join b.user u where u.isActive=true")
    Page<Business> findAvailableBusiness(Pageable pageable);
}
