package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.entity.ReservationTable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SomeHelpers {
    public static  Map<ReservationTable, BigDecimal> convertToMap(List<ReservationTable> bookingsOfParticularUser){
       return
                bookingsOfParticularUser.stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                booking -> {
                                    if (!booking.getCheckInDate().isAfter(LocalDateTime.now())) {
                                        return new BigDecimal("0");
                                    }
                                    Duration duration = Duration.between(booking.getCheckInDate(), booking.getCheckoutDate());
                                    return BookingCancellationPolicy.calculateCancellationPrice(duration, booking.getPricePerHr());
                                }
                                ,(x,y)-> {
                                    throw new IllegalStateException("duplicates reservations");
                                },LinkedHashMap::new)
                        );

    }
}
