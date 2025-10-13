package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.dto.PaginationReceiver;
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
import com.backend.hotelReservationSystem.repo.BusinessRepo;
import com.backend.hotelReservationSystem.repo.ReservationRepo;
import com.backend.hotelReservationSystem.repo.RoomRepo;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.utils.BookingCancellationPolicy;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final BusinessRepo businessRepo;
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;
    private final ReservationRepo reservationRepo;

    public Page<Room> findAvailableRooms(String businessUuid, BookingPolicy.BookingTime bookingTime, Integer pageNo){
        Pageable pageable = PageRequest.of(pageNo-1, PaginationReceiver.PAGE_SIZE);
        return roomRepo.findAvailableRoomsByUuid(businessUuid,bookingTime.getCheckInDate(),bookingTime.getCheckoutDate(),ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN,pageable);

    }

    @Transactional
    public void bookRoom(RoomBook bookRoomDto, User user, Long businessId) {
          if(bookRoomDto.getCheckInTime().isAfter(LocalDateTime.now().plusMonths(2L))){
              throw new BookingCancellationException("Cannot Book room. Booking is not allowed two or more months prior. ");
          }

//        // 1. find the room
        Room room = roomRepo.findRoomExistence(businessId, bookRoomDto.getRoomNumber(), bookRoomDto.getCheckInTime(),bookRoomDto.getCheckoutTime(),ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN)
                .orElseThrow(() -> new RoomNotFoundException("Room not found or already rented or not available for renting"));

//        2. calculate total price

        BigDecimal totalPrice = BookingPolicy.roomPrizeForDuration(
                bookRoomDto.getCheckInTime(),bookRoomDto.getCheckoutTime(),room.getPricePerHour())
                .orElseThrow(() -> new BookingCancellationException("Invalid duration provided for booking"));

        if(!(totalPrice.compareTo(bookRoomDto.getUserProvidePrice().setScale(2, RoundingMode.HALF_UP)) ==0)){
            throw new BookingCancellationException("Provided amount does not match the price provided for booking. Expected price:"+totalPrice+" ,user provided price"+bookRoomDto.getUserProvidePrice());
        }
//
//
//        // 4. deduct user balance
        boolean userSuccess = userRepo.deductUserBalance(user.getUserId(), totalPrice) > 0;
        if (!userSuccess) {
            throw new InsufficientBalanceException("Insufficient balance or user not found");
        }

//        // 5. add business balance
        boolean businessSuccess = userRepo.addBusinessBalance(businessId, totalPrice) > 0;
        if (!businessSuccess) {
            throw new RuntimeException("Business not found or balance update failed");
        }
//
//        // 6. save reservation
        ReservationTable reservation = CustomBuilder.createReservationObj(bookRoomDto, user, room,totalPrice);
        reservationRepo.save(reservation);
    }




    public Optional<Business> findBusiness(FindBusinessDto findbusinessdto) {

        return businessRepo.findBusiness(findbusinessdto.getBusinessName(),findbusinessdto.getCity(),findbusinessdto.getLocation());
    }

    public Page<Business> findAllAvailableBusinesses(Integer pageNo) {
        Pageable pageRequest = PageRequest.of(pageNo-1, PaginationReceiver.PAGE_SIZE);
        return businessRepo.findAvailableBusiness(pageRequest);
    }

    public Page<ReservationTable> findBookingsOfParticularUser(String name, Long businessId,Integer pageNo) {
        Pageable pageRequest = PageRequest.of(pageNo - 1, PaginationReceiver.PAGE_SIZE);
        Page<ReservationTable> bookingsOfParticularUser = reservationRepo.findBookingsOfParticularUser(name, businessId, ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN,LocalDateTime.now(),pageRequest);
        return bookingsOfParticularUser;



    }


    public void cancelBooking(CancelBookingDto
            roomBookingCancel, String userEmail, Long businessId) {
        Optional<ReservationTable> bookedRoomOfParticularUser = reservationRepo.findBookedRoomOfParticularUser(roomBookingCancel.getRoomNumber(),roomBookingCancel.getCheckInTime(),roomBookingCancel.getCheckoutTime(), userEmail, businessId, ReservationStatus.BOOKED);
        ReservationTable reservationTable = bookedRoomOfParticularUser.orElseThrow(() -> new BookingCancellationException("Booking Cancellation failed either due to no active booking or due to invalid credentials "));
         if(!reservationTable.getCheckInDate().isAfter(LocalDateTime.now())){
             throw new BookingCancellationException("Cannot cancel Bookings after checkedIn time");
         }

        BigDecimal pricePerHour = reservationTable.getPricePerHr();
        BigDecimal userPaidAmt = reservationTable.getPaymentAmt();
        Duration duration = Duration.between(reservationTable.getCheckInDate(), reservationTable.getCheckoutDate());
        BigDecimal priceToReturn = BookingCancellationPolicy.calculateCancellationPrice(duration, pricePerHour);
        int userSuccess = userRepo.addUserBalance(priceToReturn, userEmail);
        int businessSuccess = userRepo.deductBusinessBalance(businessId, priceToReturn);
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
    public boolean earlyCheckout(CancelBookingDto earlyCheckout,String userEmail,Long businessId){
        Optional<ReservationTable> bookedRoomOfParticularUser = reservationRepo.findBookedRoomOfParticularUser(earlyCheckout.getRoomNumber(),earlyCheckout.getCheckInTime(),earlyCheckout.getCheckoutTime(), userEmail, businessId, ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN);
        ReservationTable reservationTable = bookedRoomOfParticularUser.orElseThrow(() -> new BookingCancellationException("Early Checkout failed either due to no active booking or due to invalid credentials "));
        if(!reservationTable.getCheckInDate().isAfter(LocalDateTime.now())&&!reservationTable.getCheckoutDate().isBefore(LocalDateTime.now())){
            reservationTable.setStatus(ReservationStatus.EARLY_CHECKED_OUT);
            reservationTable.setCheckoutDate(LocalDateTime.now());
            reservationRepo.save(reservationTable);
            return true;
        }
        return false;
    }
}
