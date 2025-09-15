package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.entity.MailToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface MailRepo extends JpaRepository<MailToken,Long> {
    @Query("select t from MailToken t where t.token=?1")
    Optional<MailToken> findToken(String token);
}
