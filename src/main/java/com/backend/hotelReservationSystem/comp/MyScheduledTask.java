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
    @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 00 12 * * *")
    public void runDailyTask() {
        try {
            List<ReservationTable> cancelledAndCheckedOutRooms = reservationRepo.findRoomsWithStatusCheckoutAndCancelled(ReservationStatus.CHECKED_OUT, ReservationStatus.CANCELLED);
            List<ReservationHistory> reservationHistory = cancelledAndCheckedOutRooms.stream().map(reservationTable -> CustomBuilder.createReservationHistoryObj(reservationTable)).toList();
            if(!cancelledAndCheckedOutRooms.isEmpty()) {
                log.info("Fetched {} reservations, saving {} history entries", cancelledAndCheckedOutRooms.size(), reservationHistory.size());
                reservationHistoryRepo.saveAll(reservationHistory);
                reservationRepo.deleteAll(()->cancelledAndCheckedOutRooms.iterator());
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
