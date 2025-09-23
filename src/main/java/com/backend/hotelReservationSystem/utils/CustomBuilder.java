package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.dto.businessServiceDto.BusinessRegAcceptor;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomAcceptorDto;
import com.backend.hotelReservationSystem.dto.userServiceDto.RoomBook;
import com.backend.hotelReservationSystem.entity.*;
import com.backend.hotelReservationSystem.enums.MailStatus;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CustomBuilder {
    public static  User buildUser(String email, Role role){
        boolean isAdminApproved= (role == Role.USER);
          return User.builder()
                .email(email)
                .role(role)
                .isAdminApproved(isAdminApproved)
                .isEmailVerified(false)
                .build();

    }
    public static MailToken buildMailToken(User user,String token){
        MailToken mailToken;
        if(user.getMailToken()!=null){
            mailToken= user.getMailToken();
            mailToken.setToken(token);
            mailToken.setMailStatus(MailStatus.PENDING);
            mailToken.setExpiryDate(LocalDateTime.now().plusMinutes(10L));
        }
        else {
            mailToken = MailToken.builder()
                    .user(user)
                    .token(token)
                    .mailStatus(MailStatus.PENDING)
                    .expiryDate(LocalDateTime.now().plusMinutes(10L))
                    .build();
        }
        return mailToken;
    }
    public static Business buildBusiness(BusinessRegAcceptor businessRegAcceptor, User user){
        return Business.builder()
                .user(user)
                .businessUuid(UUID.randomUUID().toString())
                .businessName(businessRegAcceptor.getBusinessName())
                .cityName(businessRegAcceptor.getCity())
                .location(businessRegAcceptor.getLocation())
                .build();
    }
    public static Room buildRoom(RoomAcceptorDto roomAcceptorDto, Business business){
        return Room.builder()
                .roomIsActive(roomAcceptorDto.isActive())
                .roomNumber(roomAcceptorDto.getRoomNumber())
                .business(business)
                .pricePerHour(roomAcceptorDto.getPricePerHour())
                .roomType(roomAcceptorDto.getRoomType())
                .build();
    }
    public static ReservationTable createReservationObj(RoomBook bookRoomDtoPost, User user, Room room, BigDecimal totalPrice){
return  ReservationTable.builder()
        .user(user)
        .room(room)
        .paymentAmt(totalPrice)
        .pricePerHr(room.getPricePerHour())
        .bookingDate(LocalDateTime.now())
        .checkInDate(bookRoomDtoPost.getCheckInTime())
        .checkoutDate(bookRoomDtoPost.getCheckoutTime())
        .status(ReservationStatus.BOOKED)
        .build();
    }
    public static ReservationHistory createReservationHistoryObj(ReservationTable reservationTable){
       return  ReservationHistory.builder()
               .checkInDate(reservationTable.getCheckInDate())
                .createdAt(LocalDateTime.now())
                .user(reservationTable.getUser())
                .room(reservationTable.getRoom())
                .business(reservationTable.getRoom().getBusiness())
                .originalReservation(reservationTable)
               .pricePerHour(reservationTable.getPricePerHr())
                .paymentAmount(reservationTable.getPaymentAmt())
                .status(reservationTable.getStatus())
                .bookingDate(reservationTable.getBookingDate())
               .checkInDate(reservationTable.getCheckInDate())
                .checkoutDate(reservationTable.getCheckoutDate())
        .build();
    }
}
