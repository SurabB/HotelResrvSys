package com.backend.hotelReservationSystem.entity;

import com.backend.hotelReservationSystem.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class ReservationTable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name="booking_date",nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "checking_date",nullable = false)
   private LocalDateTime checkingDate;

    @Column(name = "checkout_date",nullable = false)
   private LocalDateTime checkoutDate;

   @Enumerated(EnumType.STRING)
   @Column(name = "reservation_status",nullable = false)
   private ReservationStatus status;

   @Column(name = "payment_amt",nullable = false)
   private BigDecimal paymentAmt;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name="user_id",
   foreignKey = @ForeignKey(name = "FK_Own_ReservationTable_userId__UserTable_userId"))
   private User user;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name="room_id",
           foreignKey = @ForeignKey(name = "FK_Own_ReservationTable_roomId__Room_roomId"))
  private Room room;

}
