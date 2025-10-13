package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface RoomRepo extends JpaRepository<Room,Long> {
    @Query("select r from Room r join r.business b join b.user u where b.businessUuid=:businessUuid and u.isActive=true and r.roomIsActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.checkInDate<:checkoutDate and rt.checkoutDate>:checkInDate and (rt.status=:booked or rt.status=:checkedIn))")
    Page<Room> findAvailableRoomsByUuid(String businessUuid, LocalDateTime checkInDate, LocalDateTime checkoutDate, ReservationStatus booked, ReservationStatus checkedIn, Pageable pageable);

    @Query("select r from Room r join r.business b join b.user u where u.email=:userEmail and u.isActive=true")
    Page<Room> findRoomByBusinessEmail(String userEmail, ReservationStatus reservationStatus,Pageable pageable);

    @Query("select r from Room r join r.business b join b.user u where b.businessId=:businessId and r.roomNumber=:roomNo and u.isActive=true and r.roomIsActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.checkInDate<:checkoutDate and rt.checkoutDate>:checkInDate and (rt.status=:booked or rt.status=:checkedIn))")
    Optional<Room> findRoomExistence(Long businessId, Long roomNo, LocalDateTime checkInDate, LocalDateTime checkoutDate, ReservationStatus booked, ReservationStatus checkedIn);


    @Query("select case when count(r)>0 then true else false end from Room r join r.business.user u where u.email=:businessEmail and r.roomNumber=:roomNumber")
    boolean existRoomByRoomNumberAndBusinessEmail(Long roomNumber, String businessEmail);


    @Query("select r from Room r join r.business b join b.user u where r.roomNumber=:roomNumber and u.email=:businessEmail and u.isActive=true")
    Optional<Room> findParticularRoomByBusinessEmail(Long roomNumber, String businessEmail);

    @Query("select rt from ReservationTable rt join fetch rt.room r where r.business.user.email = :businessEmail and (rt.status = :booked or rt.status = :checkedIn) and rt.checkoutDate>now")
    Page<ReservationTable> findBookedReservationAndRooms(String businessEmail, ReservationStatus booked, ReservationStatus checkedIn,LocalDateTime now, Pageable pageable);
}
