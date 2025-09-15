package com.backend.hotelReservationSystem.comp;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.entity.ReservationHistory;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.repo.ReservationHistoryRepo;
import com.backend.hotelReservationSystem.repo.ReservationRepo;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
@AllArgsConstructor
public class MyScheduledTask {
    private final ReservationRepo reservationRepo;
    private final ReservationHistoryRepo reservationHistoryRepo;

    @Transactional
//    @Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 00 01 * * *")
    public void runDailyTask() {
        try {
            List<ReservationTable> allExceptBookedStatus = reservationRepo.findAllExceptBookedStatus(ReservationStatus.CHECKED_OUT, ReservationStatus.CANCELLED);
            List<ReservationHistory> reservationHistory = allExceptBookedStatus.stream().map(reservationTable -> CustomBuilder.createReservationHistoryObj(reservationTable)).toList();
            if(!reservationHistory.isEmpty()) {
                log.info("Fetched {} reservations, saving {} history entries", allExceptBookedStatus.size(), reservationHistory.size());
                reservationHistoryRepo.saveAll(reservationHistory);
            }
            else {
                log.info("No reservations Today");
            }
        }
        catch (Exception e) {
            log.error("error occurred while saving reservation history ,error:{}", e.getMessage());
        }
    }
}
