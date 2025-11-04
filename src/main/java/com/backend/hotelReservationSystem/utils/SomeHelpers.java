package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.controller.actualcontrollers.ImageController;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SomeHelpers {
    public static final Set<String> ALLOWED_IMG_TYPE=Set.of("jpeg","png","jpg","webp");

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
    public static boolean isImageTypeValid(MultipartFile file, Tika tika) throws IOException {
        String fileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        String detectedType = tika.detect(file.getInputStream());

        if (!detectedType.contains("image/") || !ALLOWED_IMG_TYPE.contains(extension)) {
           return false;
        }
        return true;
    }
}
