package com.backend.hotelReservationSystem;

import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class HotelReservationSystemApplication implements CommandLineRunner {
private final PasswordEncoder passwordEncoder;
private  final UserRepo userRepo;
@Value("${default.admin.email}")
private String adminEmail;
	public static void main(String[] args) {
		SpringApplication.run(HotelReservationSystemApplication.class, args);
	}

	@Override
	public void run(String... args) {
		User user = User.builder()
				.email(adminEmail)
				.role(Role.ADMIN)
				.password(passwordEncoder.encode("password"))
				.isAdminApproved(true)
				.isEmailVerified(true)
				.bankBalance(new BigDecimal("1200"))
				.build();
		try {
			userRepo.save(user);
		}
		catch (Exception e){
            log.warn("failed to add default admin,reason:{}", e.getMessage());
		}
	}
}
