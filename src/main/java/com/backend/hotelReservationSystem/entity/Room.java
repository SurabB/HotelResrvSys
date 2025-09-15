package com.backend.hotelReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="room",
uniqueConstraints = @UniqueConstraint(
        name="UK_businessId_roomNumber",
        columnNames = {"business_id", "room_number"}
))
public class Room {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
            @Column(name = "room_id")
    Long roomId;

    @Column(nullable = false,name = "room_number")
    Long roomNumber;

    @Column(nullable = false,
            name = "price_per_hour",
            precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(nullable = false,name = "room_type")
    private  String roomType;

    @Column(nullable = false,name = "room_is_active")
    private  Boolean roomIsActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="business_id",
    foreignKey = @ForeignKey(name="FK_Own_Room_businessId__Business_businessId"))
    private Business business;

    @OneToMany(mappedBy = "room")
    private List<ReservationTable> reservation;
}
