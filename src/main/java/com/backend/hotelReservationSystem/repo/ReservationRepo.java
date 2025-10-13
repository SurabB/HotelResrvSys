package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReservationRepo extends JpaRepository<ReservationTable,Long> {

@Query("select rt from ReservationTable rt join rt.user u  join fetch  rt.room r join r.business b where u.email=:userEmail and b.businessId=:businessId and (rt.status=:booked or rt.status=:checkedIn) and rt.checkoutDate>now")
Page<ReservationTable> findBookingsOfParticularUser(String userEmail, Long businessId, ReservationStatus booked, ReservationStatus checkedIn,LocalDateTime now, Pageable pageable);

@Query("select rt from ReservationTable rt join fetch rt.room r join rt.user u join r.business b where b.businessId=:businessId and r.roomNumber=:roomNo and u.email=:userEmail and b.user.isActive=true and rt.status=:booked and rt.checkInDate=:checkInDate and rt.checkoutDate=:checkoutDate")
    Optional<ReservationTable> findBookedRoomOfParticularUser(Long roomNo,LocalDateTime checkInDate,LocalDateTime checkoutDate, String userEmail, Long businessId,ReservationStatus booked);

    @Query("select rt from ReservationTable rt join fetch rt.room r join rt.user u join r.business b where b.businessId=:businessId and r.roomNumber=:roomNo and u.email=:userEmail and b.user.isActive=true and (rt.status=:booked or rt.status=:checkedIn)and rt.checkInDate=:checkInDate and rt.checkoutDate=:checkoutDate")
    Optional<ReservationTable> findBookedRoomOfParticularUser(Long roomNo,LocalDateTime checkInDate,LocalDateTime checkoutDate, String userEmail, Long businessId,ReservationStatus booked,ReservationStatus checkedIn);

    @Query("select rt from ReservationTable rt join fetch rt.user u join fetch rt.room r join fetch r.business b where rt.status not in (:checkedIn ,:booked)")
    List<ReservationTable> findRoomsWithStatusExceptCheckedInAndBooked(ReservationStatus checkedIn,
                                                                   ReservationStatus booked);
}
