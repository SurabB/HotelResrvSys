package com.backend.hotelReservationSystem.entity;

import com.backend.hotelReservationSystem.enums.MailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="mail_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_MailToken_token",
                        columnNames = "token"
                ),
                @UniqueConstraint(
                        name = "UK_MailToken_userId",
                        columnNames = "user_id"
                )
        }
)
public class MailToken {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="token_id")
   private Long tokenId;

    @Column(nullable = false,
    unique = true,
    name="token")
    private String token;

    @Column(nullable = false,
    name="expiry_date")
    private LocalDateTime expiryDate;

    @Column(nullable = false,
            name="mailStatus")
    @Enumerated(EnumType.STRING)
    private MailStatus mailStatus;

    @OneToOne
    @JoinColumn(name="user_id",
            foreignKey = @ForeignKey(name = "FK_Own_MailToken_userId__UserTable_userId")
    )
    private User user;



}
