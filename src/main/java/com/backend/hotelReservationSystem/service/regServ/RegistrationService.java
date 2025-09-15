package com.backend.hotelReservationSystem.service.regServ;

import com.backend.hotelReservationSystem.enums.MailStatus;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.entity.MailToken;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import com.backend.hotelReservationSystem.utils.TokenCreation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Service
public class RegistrationService {
private final TokenService tokenService;
private final CustomMailSender customMailSender;
private final UserRepo userRepo;

    public void userTokenRegistration(User user){

        //creates random token
        String token= TokenCreation.generateToken();

        //builds token obj to save in db.
        MailToken mailToken = CustomBuilder.buildMailToken(user, token);

        // saves user  and token obj in db
        User dbUser = tokenService.registerUserAndToken(user, mailToken);

        // actual token sent to user
        String userVerificationToken=dbUser.getUserId()+TokenCreation.getSeparator()+token;

        //sends mail to user
        customMailSender.sendMail(dbUser,userVerificationToken);

    }

    public void userTokenRegistration(String email, Role role) {
        //checks if user exist in db
        Optional<User> userFromDb = userRepo.findUserByEmail(email);

        //if exist get it else create new one
        User user = userFromDb.orElse(CustomBuilder.buildUser(email,role));

      //newly created user will pass the provided condition
        /*for old users ,only those users which email is not verified
          and
            either their mailToken(obj of token) is null->user got registered but didn't verify
            their email.
            or their token has expired
            or mailStatus is pending->this occurs when failed to send mail due to exception
         */


        if(!user.getIsEmailVerified()&&(user.getMailToken()==null
                ||user.getMailToken().getExpiryDate().isBefore(LocalDateTime.now())
                ||user.getMailToken().getMailStatus()== MailStatus.PENDING)){

            //if satisfies user is saved in db and email is sent to provided email
            userTokenRegistration(user);


        }
    }


}
