package com.backend.hotelReservationSystem.entity;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservation_history")
public class ReservationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;


    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime bookingDate;
    private LocalDateTime checkInDate;
    private LocalDateTime checkoutDate;
   private BigDecimal pricePerHour;
    private BigDecimal paymentAmount;

    private LocalDateTime createdAt;
}

