package com.backend.hotelReservationSystem.entity;

import com.backend.hotelReservationSystem.enums.Role;
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
@Table(name = "user_table",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_UserTable_email",
                        columnNames = "email"
                )
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long userId;

    @Column(name="email",
    nullable = false,
    unique = true)
    private  String email;

    @Column(name="password")
    private  String password;

    @Column(name="role",
    nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name="is_admin_approved",
            nullable = false)
    Boolean isAdminApproved;

    @Column(name="is_email_verified",
            nullable = false)
   private Boolean isEmailVerified;


    @Column(name="is_active",
            insertable = false,
             updatable = false
            )
    private Boolean isActive;


    @Column(name = "bank_balance",
            precision = 10, scale = 2)
    private BigDecimal bankBalance;

    @OneToOne(mappedBy = "user",cascade={
            CascadeType.ALL
    },orphanRemoval = true)
    private MailToken mailToken;

    @OneToOne(mappedBy = "user",cascade = CascadeType.PERSIST)
    private Business business;

    @OneToMany(mappedBy = "user")
    private List<ReservationTable> reservations;
}
