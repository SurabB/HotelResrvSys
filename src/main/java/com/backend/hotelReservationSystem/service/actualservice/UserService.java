package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.dto.userServiceDto.BookRoomDto;
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

    public List<Room> findAvailableRooms(String businessUuid){
        return reservationRepo.findAvailableRoomsByUuid(businessUuid, ReservationStatus.BOOKED);

    }

    @Transactional
    public void bookRoom(BookRoomDto bookRoomDto, User user,String businessUuid) {

        // 1. find the room
        Room room = reservationRepo.findRoomExistence(businessUuid, bookRoomDto.getRoomNumber(), ReservationStatus.BOOKED)
                .orElseThrow(() -> new RoomNotFoundException("Room not found or already booked or not available for renting"));

        // 2. calculate total price and check user balance
        BigDecimal totalPrice = calculateTotalPriceIfUserHasBalance(
                room.getPricePerHour(), bookRoomDto.getBookingTime(), user.getBankBalance())
                .orElseThrow(() -> new InsufficientBalanceException("Insufficient balance in bank"));

        // 3. get business id
        Long businessId = room.getBusiness().getUser().getUserId();

        // 4. deduct user balance
        boolean userSuccess = reservationRepo.deductUserBalance(user.getUserId(), totalPrice) > 0;
        if (!userSuccess) {
            throw new InsufficientBalanceException("Insufficient balance or user not found");
        }

        // 5. add business balance
        boolean businessSuccess = reservationRepo.addBusinessBalance(businessId, totalPrice) > 0;
        if (!businessSuccess) {
            throw new RuntimeException("Business not found or balance update failed");
        }

        // 6. save reservation
        ReservationTable reservation = CustomBuilder.createReservationObj(bookRoomDto.getBookingTime(), user, room,totalPrice);
        reservationRepo.save(reservation);
    }

    private Optional<BigDecimal> calculateTotalPriceIfUserHasBalance(
            BigDecimal pricePerHour, Integer bookingTimeInHrs, BigDecimal userBalance) {
    // 1 calculates total price for room reservation
        BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(bookingTimeInHrs));

    // 2 returns total price if user has sufficient balance otherwise returns an empty optional
        return totalPrice.compareTo(userBalance) <= 0
                ? Optional.of(totalPrice)
                : Optional.empty();
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
                    LocalDateTime bookingDate = booking.getBookingDate();
                    LocalDateTime checkoutDate = booking.getCheckoutDate();
                    Duration duration = Duration.between(bookingDate, checkoutDate);
                    BigDecimal pricePerHour = booking.getRoom().getPricePerHour();
                    BigDecimal paymentAmt = booking.getPaymentAmt();
                    BigDecimal refundableAmt = BookingCancellationPolicy.calculateCancellationPrice(duration, pricePerHour);
                    map.put(booking,refundableAmt);
                }
        );
         return map;

    }

    public void cancelBooking(Long roomNo, String userEmail, Long businessId) {
        Optional<ReservationTable> bookedRoomOfParticularUser = reservationRepo.findBookedRoomOfParticularUser(roomNo, userEmail, businessId, ReservationStatus.BOOKED);
        ReservationTable reservationTable = bookedRoomOfParticularUser.orElseThrow(() -> new BookingCancellationException("Booking failed either due to no active booking or due to invalid credentials "));
        LocalDateTime checkoutDate = reservationTable.getCheckoutDate();
        LocalDateTime now = LocalDateTime.now();
        if (!checkoutDate.isAfter(now.plusHours(1))) {
            throw new BookingCancellationException("Cannot cancel booking within 1 hour of checkout or after checkout.");
        }
        Room room = reservationTable.getRoom();
        BigDecimal pricePerHour = room.getPricePerHour();
        BigDecimal userPaidAmt = reservationTable.getPaymentAmt();

        //compare checkoutDate and now() and retrieve no.of hours left with minutes
        Duration duration = Duration.between(now, checkoutDate);

        BigDecimal priceToReturn = BookingCancellationPolicy.calculateCancellationPrice(duration, pricePerHour);
        int userSuccess = reservationRepo.addUserBalance(priceToReturn, userEmail);
        int businessSuccess = reservationRepo.deductBusinessBalance(businessId, priceToReturn);
        if (userSuccess!=1){
            throw new RuntimeException("something went wrong while adding to user balance while booking cancellation");
        }
        if (businessSuccess!=1){
            throw new BookingCancellationException("something went wrong while booking cancellation");
        }
        reservationTable.setStatus(ReservationStatus.CANCELLED);
        reservationTable.setCheckoutDate(LocalDateTime.now());
        reservationTable.setPaymentAmt(userPaidAmt.subtract(priceToReturn));
        reservationRepo.save(reservationTable);

    }
}
