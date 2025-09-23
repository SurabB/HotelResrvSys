package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.dto.userServiceDto.CancelBookingDto;
import com.backend.hotelReservationSystem.dto.userServiceDto.RoomBook;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.dto.userServiceDto.FindBusinessDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.BookingCancellationException;
import com.backend.hotelReservationSystem.exceptionClasses.InsufficientBalanceException;
import com.backend.hotelReservationSystem.exceptionClasses.RoomNotFoundException;
import com.backend.hotelReservationSystem.repo.ReservationRepo;
import com.backend.hotelReservationSystem.utils.BookingCancellationPolicy;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final ReservationRepo reservationRepo;

    public List<Room> findAvailableRooms(String businessUuid, BookingPolicy.BookingTime bookingTime){
        return reservationRepo.findAvailableRoomsByUuid(businessUuid,bookingTime.getCheckInDate(),bookingTime.getCheckoutDate(),ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN);

    }

    @Transactional
    public void bookRoom(RoomBook bookRoomDto, User user, Long businessId) {
          if(bookRoomDto.getCheckInTime().isAfter(LocalDateTime.now().plusMonths(2L))){
              throw new BookingCancellationException("Cannot Book room. Booking is not allowed two or more months prior. ");
          }

//        // 1. find the room
        Room room = reservationRepo.findRoomExistence(businessId, bookRoomDto.getRoomNumber(), bookRoomDto.getCheckInTime(),bookRoomDto.getCheckoutTime(),ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN)
                .orElseThrow(() -> new RoomNotFoundException("Room not found or already rented or not available for renting"));
//
//        2. calculate total price

        BigDecimal totalPrice = BookingPolicy.roomPrizeForDuration(
                bookRoomDto.getCheckInTime(),bookRoomDto.getCheckoutTime(),room.getPricePerHour())
                .orElseThrow(() -> new BookingCancellationException("Invalid duration provided for booking"));


//
//
//        // 4. deduct user balance
        boolean userSuccess = reservationRepo.deductUserBalance(user.getUserId(), totalPrice) > 0;
        if (!userSuccess) {
            throw new InsufficientBalanceException("Insufficient balance or user not found");
        }

//        // 5. add business balance
        boolean businessSuccess = reservationRepo.addBusinessBalance(businessId, totalPrice) > 0;
        if (!businessSuccess) {
            throw new RuntimeException("Business not found or balance update failed");
        }
//
//        // 6. save reservation
        ReservationTable reservation = CustomBuilder.createReservationObj(bookRoomDto, user, room,totalPrice);
        reservationRepo.save(reservation);
    }




    public Optional<Business> findBusiness(FindBusinessDto findbusinessdto) {

        return reservationRepo.findBusiness(findbusinessdto.getBusinessName(),findbusinessdto.getCity(),findbusinessdto.getLocation());
    }

    public List<Business> findAllAvailableBusinesses() {
        return reservationRepo.findAvailableBusiness();
    }

    public HashMap<ReservationTable,BigDecimal> findBookingsOfParticularUser(String name, Long businessId) {
        List<ReservationTable> bookingsOfParticularUser = reservationRepo.findBookingsOfParticularUser(name, businessId, ReservationStatus.BOOKED);
        HashMap<ReservationTable,BigDecimal> map=new HashMap<>();
         bookingsOfParticularUser.forEach(booking -> {
                    Duration duration = Duration.between(booking.getCheckInDate(), booking.getCheckoutDate());
                    BigDecimal pricePerHour = booking.getRoom().getPricePerHour();
                    BigDecimal refundableAmt = BookingCancellationPolicy.calculateCancellationPrice(duration, pricePerHour);
                    map.put(booking,refundableAmt);
                }
        );
         return map;

    }

    public void cancelBooking(CancelBookingDto
            roomBookingCancel, String userEmail, Long businessId) {
        Optional<ReservationTable> bookedRoomOfParticularUser = reservationRepo.findBookedRoomOfParticularUser(roomBookingCancel.getRoomNumber(),roomBookingCancel.getCheckInTime(),roomBookingCancel.getCheckoutTime(), userEmail, businessId, ReservationStatus.BOOKED);
        ReservationTable reservationTable = bookedRoomOfParticularUser.orElseThrow(() -> new BookingCancellationException("Booking failed either due to no active booking or due to invalid credentials "));
        BigDecimal pricePerHour = reservationTable.getPricePerHr();
        BigDecimal userPaidAmt = reservationTable.getPaymentAmt();
        Duration duration = Duration.between(reservationTable.getCheckInDate(), reservationTable.getCheckoutDate());

        BigDecimal priceToReturn = BookingCancellationPolicy.calculateCancellationPrice(duration, pricePerHour);
        int userSuccess = reservationRepo.addUserBalance(priceToReturn, userEmail);
        int businessSuccess = reservationRepo.deductBusinessBalance(businessId, priceToReturn);
        if (userSuccess!=1){
            throw new RuntimeException("something went wrong");
        }
        if (businessSuccess!=1){
            throw new BookingCancellationException("something went wrong");
        }
        reservationTable.setStatus(ReservationStatus.CANCELLED);
        reservationTable.setCheckoutDate(LocalDateTime.now());
        reservationTable.setPaymentAmt(userPaidAmt.subtract(priceToReturn));
        reservationRepo.save(reservationTable);

    }
}
