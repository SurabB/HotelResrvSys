package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import jakarta.transaction.Transactional;
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
    List<Room> findAvailableRoomsByUuid(String businessUuid, LocalDateTime checkInDate, LocalDateTime checkoutDate, ReservationStatus booked, ReservationStatus checkedIn);

    @Query("select r from Room r join r.business b join b.user u where u.email=:userEmail and u.isActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.status=:reservationStatus)")
    List<Room> findAvailableRoomsByEmail(String userEmail, ReservationStatus reservationStatus);

    @Query("select r from Room r join r.business b join b.user u where b.businessId=:businessId and r.roomNumber=:roomNo and u.isActive=true and r.roomIsActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.checkInDate<:checkoutDate and rt.checkoutDate>:checkInDate and (rt.status=:booked or rt.status=:checkedIn))")
    Optional<Room> findRoomExistence(Long businessId, Long roomNo, LocalDateTime checkInDate, LocalDateTime checkoutDate, ReservationStatus booked, ReservationStatus checkedIn);


    @Modifying
@Query("update Room r set r.roomIsActive= CASE when r.roomIsActive = true then false else true END where r.business.user.email=:email and r.business.user.isActive=true and r.roomNumber=:roomNumber")
    int changeActiveStatus(String email,Long roomNumber);

    @Query("select r from Room r join r.business b join b.user u where u.email=:userEmail and  u.isActive=true")
    List<Room> getAllRooms(String userEmail);

    @Query("select case when count(r)>0 then true else false end from Room r join r.business.user u where u.email=:businessEmail and r.roomNumber=:roomNumber")
    boolean existRoomByRoomNumberAndBusinessEmail(Long roomNumber, String businessEmail);


    @Query("select r from Room r join r.business b join b.user u where r.roomNumber=:roomNumber and u.email=:businessEmail and u.isActive=true and not exists (select 1 from ReservationTable rt where  rt.room=r and rt.status=:reservationStatus)")
    Optional<Room> findParticularRoomByBusinessEmail(Long roomNumber, String businessEmail, ReservationStatus reservationStatus);

    @Query("select r from Room r join fetch  r.reservation rt where rt.room=r and r.business.user.email=:businessEmail and rt.status=:reservationStatus")
    List<Room> findBookedReservationAndRooms(String businessEmail,ReservationStatus reservationStatus);
}
