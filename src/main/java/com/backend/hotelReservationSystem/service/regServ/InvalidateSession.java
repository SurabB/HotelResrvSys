package com.backend.hotelReservationSystem.service.regServ;

import lombok.AllArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InvalidateSession {

    private final SessionRegistry sessionRegistry;

    public void expireUserSessionsByEmail(String email) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails user) {
                if (user.getUsername().equals(email)) {
                    for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(principal, false)) {
                        sessionInfo.expireNow();
                    }
                }
            }
        }
    }

}
