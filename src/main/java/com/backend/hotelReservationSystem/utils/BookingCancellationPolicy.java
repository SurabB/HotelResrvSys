package com.backend.hotelReservationSystem.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingCancellationPolicy {
    private static final  BigDecimal PENALTY_PERCENT=new BigDecimal(5);
    public static BigDecimal calculateCancellationPrice(Duration duration,
                                                 BigDecimal pricePerHour) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        // hours * pricePerHour
        BigDecimal hoursPart = pricePerHour.multiply(BigDecimal.valueOf(hours));

        // (minutes / 60) * pricePerHour
        BigDecimal minutesFraction = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal minutesPart = pricePerHour.multiply(minutesFraction);

        // total price
        BigDecimal amtToReturn = hoursPart.add(minutesPart);

        // penaltyAmt = (penaltyPercent / 100) * amountToReturn
        BigDecimal penaltyAmt = amtToReturn.multiply(
                PENALTY_PERCENT.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        // final price
        return amtToReturn.subtract(penaltyAmt);
    }
}
