package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReservationRepo extends JpaRepository<ReservationTable,Long> {
  @Query("select r from Room r join r.business b join b.user u where b.businessUuid=:businessUuid and u.isActive=true and r.roomIsActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.checkInDate<:checkoutDate and rt.checkoutDate>:checkInDate)")
    List<Room> findAvailableRoomsByUuid(String businessUuid, LocalDateTime checkInDate,LocalDateTime checkoutDate);

    @Query("select r from Room r join r.business b join b.user u where u.email=:userEmail and u.isActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.status=:reservationStatus)")
    List<Room> findAvailableRoomsByEmail(String userEmail, ReservationStatus reservationStatus);

    @Query("select r from Room r join r.business b join b.user u where b.businessUuid=:businessUuid and r.roomNumber=:roomNo and u.isActive=true and r.roomIsActive=true and not exists (select 1 from ReservationTable rt where rt.room=r and rt.status=:reservationStatus)")
    Optional<Room> findRoomExistence(String businessUuid, Long roomNo, ReservationStatus reservationStatus);


    @Modifying
    @Transactional
    @Query("update User u set u.bankBalance = u.bankBalance - :totalPrice where u.userId = :userId and u.bankBalance>=:totalPrice")
    int deductUserBalance(Long userId, BigDecimal totalPrice);

    @Modifying
    @Transactional
    @Query("update User u set u.bankBalance = u.bankBalance + :totalPrice  where u.userId = :businessId")
    int addBusinessBalance(Long businessId, BigDecimal totalPrice);

    @Query("select b from Business b join b.user u where b.businessName=:businessName and b.cityName=:city and b.location=:location and u.isActive=true")
    Optional<Business> findBusiness(String businessName, String city, String location);

    @Query("select b from Business b join b.user u where u.isActive=true")
    List<Business> findAvailableBusiness();

@Query("select rt from ReservationTable rt join rt.user u  join fetch  rt.room r join r.business b where u.email=:userEmail and b.businessId=:businessId and rt.status=:reservationStatus")
    List<ReservationTable> findBookingsOfParticularUser(String userEmail, Long businessId, ReservationStatus reservationStatus);

@Query("select rt from ReservationTable rt join fetch rt.room r join rt.user u join r.business b where b.businessId=:businessId and r.roomNumber=:roomNo and u.email=:userEmail and b.user.isActive=true and rt.status=:status ")
    Optional<ReservationTable> findBookedRoomOfParticularUser(Long roomNo, String userEmail, Long businessId,ReservationStatus status);

@Modifying
@Query("update User u set u.bankBalance=u.bankBalance+:priceToReturn where u.email=:userEmail and u.isActive=true")
    int addUserBalance(BigDecimal priceToReturn, String userEmail);


@Modifying
    @Query("update User u set u.bankBalance=u.bankBalance-:priceToReturn where u.business.businessId=:businessId and u.isActive=true")
    int deductBusinessBalance(Long businessId, BigDecimal priceToReturn);

    @Query("select rt from ReservationTable rt join fetch rt.user u join fetch u.business b join fetch rt.room r where (rt.status = :reservationStatus or rt.status = :reservationStatus1) and not exists (select 1 from ReservationHistory rh where rh.originalReservation = rt)")
    List<ReservationTable> findAllExceptBookedStatus(ReservationStatus reservationStatus,
                                                     ReservationStatus reservationStatus1);

    @Query("select r from Room r join r.business b join b.user u where r.roomNumber=:roomNumber and u.email=:businessEmail and u.isActive=true and not exists (select 1 from ReservationTable rt where  rt.room=r and rt.status=:reservationStatus)")
    Optional<Room> findParticularRoomByBusinessEmail(Long roomNumber, String businessEmail, ReservationStatus reservationStatus);

    @Query("select r from Room r join fetch  r.reservation rt where rt.room=r and r.business.user.email=:businessEmail and rt.status=:reservationStatus")
    List<Room> findBookedReservationAndRooms(String businessEmail,ReservationStatus reservationStatus);
}
