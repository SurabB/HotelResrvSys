package com.backend.hotelReservationSystem.service.regServ;

import com.backend.hotelReservationSystem.enums.MailStatus;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;



@Service
@AllArgsConstructor
public class CustomMailSender {
    private JavaMailSender javaMailSender;
    private UserRepo userRepo;

    public void sendMail(User user,String token){
        System.out.println("token:"+token);
        String msg = roleBasedMessage(user, token);
        String subject="Sent from SpringBootApp_HotelReservationSystem";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(msg);
        javaMailSender.send(message);
        user.getMailToken().setMailStatus(MailStatus.DELIVERED);
        userRepo.save(user);
    }
    private String roleBasedMessage(User user,String token){

        if(user.getRole()== Role.USER&&!user.getIsEmailVerified()){
         return  "Verification Token for registration is:"+token;
        }
        if(user.getRole()!=Role.USER&&!user.getIsEmailVerified()){
            return "Verification Token for registration is:"+token+".\nNote: After email verification,Account need to get approved by admin for login";
        }
        return "Password reset token:"+token;
    }
}
