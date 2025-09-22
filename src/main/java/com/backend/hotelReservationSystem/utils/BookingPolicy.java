package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.dto.userServiceDto.BookRoomDto;
import com.backend.hotelReservationSystem.exceptionClasses.BookingCancellationException;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingPolicy {
    public static BookingTime getTime(BookRoomDto bookRoomDto){
        LocalDate bookingTime = bookRoomDto.getBookingTime();

        LocalDateTime checkInTime = parseTime(bookRoomDto.getStartingTime(), bookRoomDto.getBookingTime());
        LocalDateTime checkoutTime=parseTime(bookRoomDto.getEndingTime(),bookRoomDto.getBookingTime());
        if(checkInTime.isAfter(checkoutTime)||checkoutTime.isAfter(bookRoomDto.getBookingTime().atStartOfDay().plusHours(24))){
            throw new BookingCancellationException("Invalid time limit provided. Time limit must be valid for a particular day.");
        }
        Duration duration = Duration.between(checkInTime, checkoutTime);
        if(duration.toHours()<1){
            throw new BookingCancellationException("Booking can be made for duration of more than 1 hour only");

        }
        return new BookingTime(checkInTime,checkoutTime);


    }
    public static LocalDateTime parseTime(String time,LocalDate localDate){
        try {
            int split = time.indexOf(":");
            int hour = Integer.parseInt(time.substring(0, split));
            int minutes = Integer.parseInt(time.substring(split + 1, split + 3));
            String meridiem = time.substring(split + 3).toLowerCase();

            LocalDateTime localDateTime;
            if (meridiem.equals("am")) {
                hour=(hour==12)?0:hour;
                localDateTime=LocalDateTime.of(localDate, LocalTime.of(hour, minutes, 0));
            } else {
                int incr=(hour<12)?12:0;
                localDateTime=LocalDateTime.of(localDate , LocalTime.of(hour+incr , minutes, 0));
            }
            return localDateTime;
        }
        catch(Exception ex){
            throw new BookingCancellationException("Invalid credentials provided");
        }


    }
    public static Optional<BigDecimal> roomPrizeForDuration(LocalDateTime startingTime,LocalDateTime endingTime,BigDecimal prizePerHour){
        if(startingTime.isAfter(endingTime)||Duration.between(startingTime,endingTime).toHours()<1){
            return Optional.empty();
        }
        Duration duration=Duration.between(startingTime,endingTime);
        BigDecimal hours=BigDecimal.valueOf(duration.toHours());
        BigDecimal minutes=BigDecimal.valueOf(duration.toMinutes());
        BigDecimal totalPrize=hours.multiply(prizePerHour).add(minutes.divide(BigDecimal.valueOf(60l)).multiply(prizePerHour));
        return Optional.of(totalPrize);

    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class BookingTime{
        private final LocalDateTime checkInDate;
        private  final LocalDateTime checkoutDate;
    }
}
