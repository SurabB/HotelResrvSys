package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.Room;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@Transactional
public interface RoomRepo extends JpaRepository<Room,Long> {

    @Modifying
@Query("update Room r set r.roomIsActive= CASE when r.roomIsActive = true then false else true END where r.business.user.email=:email and r.business.user.isActive=true and r.roomNumber=:roomNumber")
    int changeActiveStatus(String email,Long roomNumber);

    @Query("select r from Room r join r.business b join b.user u where u.email=:userEmail and  u.isActive=true")
    List<Room> getAllRooms(String userEmail);

    @Query("select case when count(r)>0 then true else false end from Room r join r.business.user u where u.email=:businessEmail and r.roomNumber=:roomNumber")
    boolean existRoomByRoomNumberAndBusinessEmail(Long roomNumber, String businessEmail);
}
