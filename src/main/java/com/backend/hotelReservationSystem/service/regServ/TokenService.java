package com.backend.hotelReservationSystem.service.regServ;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.dto.commonDto.VerificationTokenAcceptor;
import com.backend.hotelReservationSystem.entity.MailToken;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.TokenInvalidException;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.utils.SortingFields;
import com.backend.hotelReservationSystem.utils.TokenCreation;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TokenService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;


    public User registerUserAndToken(User user, MailToken token) {
            token.setToken(passwordEncoder.encode(token.getToken()));
            user.setMailToken(token);
           return userRepo.save(user);
    }

    public User verifyEmailAndSetPassword(VerificationTokenAcceptor verificationTokenAcceptor) {
        //parsedToken->
        TokenCreation.ParsedToken parsedToken= TokenCreation.getToken(verificationTokenAcceptor.getVerificationToken());
        Optional<User> dbUser = userRepo.findById(parsedToken.getKey());
        User actualUser = dbUser
                .filter(user -> user.getMailToken() != null &&
                user.getMailToken().getExpiryDate()
                        .isAfter(LocalDateTime.now()))
                        .orElseThrow(()->
                                new TokenInvalidException("provided token is invalid or expired"));

        String encodedToken = actualUser.getMailToken().getToken();
        boolean matches = passwordEncoder.matches(parsedToken.getUserToken(), encodedToken);
        if(!matches) {
           throw  new TokenInvalidException("provided token is invalid") ;
        }

        actualUser.setPassword(passwordEncoder.encode(verificationTokenAcceptor.getPassword()));
        actualUser.setBankBalance(BigDecimal.valueOf(200 + (Math.random() * 1000)));
        actualUser.setMailToken(null);
        actualUser.setIsEmailVerified(true);
       return userRepo.save(actualUser);

    }
public Page<User> getAllUnapprovedUsers(PageSortReceiver pageSortReceiver){
    Pageable pageRequest = SortingFields.getPageableObj(pageSortReceiver,SortingFields.Approve_UNAPPROVE_USERS);
    return userRepo.findUnapprovedUsers(pageRequest);
}
public  Page<User> getAllApprovedUsers(PageSortReceiver pageSortReceiver){
    Pageable pageRequest = SortingFields.getPageableObj(pageSortReceiver,SortingFields.Approve_UNAPPROVE_USERS);
    return userRepo.findApprovedUsers(Role.ADMIN,pageRequest);
}

    public boolean adminApproval(String email) {
        int adminApproved = userRepo.approveUserByEmail(email);
        return adminApproved >0;

    }



    public boolean disproveUser(String email) {
        int removedAdminApproval = userRepo.removeAdminApproval(email, Role.ADMIN);
        return removedAdminApproval>0;
    }

    }



