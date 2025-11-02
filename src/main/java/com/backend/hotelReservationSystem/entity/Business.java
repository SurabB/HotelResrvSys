package com.backend.hotelReservationSystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "business",
        uniqueConstraints = @UniqueConstraint(
                name="UK_city_business_location",
                columnNames = {"business_name","city_name","location"}

        )

)
public class Business {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="business_id")
    private Long businessId;

    @Column(name = "business_uuid",nullable = false)
    private String businessUuid;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name="city_name",nullable = false)
    private String cityName;

    @Column(name="location",nullable = false)
    private String location;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Image image;


    @OneToOne
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "FK_Own_Business_userId__UserTable_userId")
    )
    private User user;

    @OneToMany(mappedBy = "business")
    private List<Room> room;
}
