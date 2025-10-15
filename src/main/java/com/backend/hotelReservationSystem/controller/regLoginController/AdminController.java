package com.backend.hotelReservationSystem.controller.regLoginController;

import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import com.backend.hotelReservationSystem.dto.adminDto.UserReceiverDto;
import com.backend.hotelReservationSystem.dto.commonDto.EmailDto;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.RegistrationFailedException;
import com.backend.hotelReservationSystem.service.regServ.InvalidateSession;
import com.backend.hotelReservationSystem.service.regServ.RegistrationService;
import com.backend.hotelReservationSystem.service.regServ.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/resource")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminController {
    private final RegistrationService registrationService;
    private final TokenService tokenService;
    private final InvalidateSession invalidateSession;

    @GetMapping("/reg")
    public String adminRegistration() {
        return "adminService/adminReg";
    }

    @PostMapping("/reg")
    public String adminRegistration(@Valid @ModelAttribute EmailDto emaildto, BindingResult bindingResult ,RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/admin/resource/reg", bindingResult);
        }
        try{
            //registers user ,creates token, and sends mail with tokenCode to provided email
            registrationService.userTokenRegistration(emaildto.getEmail(), Role.ADMIN);
        redirectAttributes.addFlashAttribute("success", "If the entered email is not already verified , an email has been sent with instructions of how to verify email");
        return "redirect:/common/resource/verifyEmail";
    }
    catch(Exception e){
        throw new RegistrationFailedException("redirect:/common/resource/reg",e);
        }

    }
    @GetMapping("/adminApprove")
    public String adminApprove(@RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,Model model,RedirectAttributes redirectAttributes) {
        //fetches all unapproved (adminApproval->false) users from db  for possible admin approval
        Page<User> unApprovedUsersPageFromDb = tokenService.getAllUnapprovedUsers(pageNo);
        int totalPages=unApprovedUsersPageFromDb.getTotalPages();

        //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
        if(totalPages>0&& totalPages<pageNo){
            redirectAttributes.addAttribute("pageNo",totalPages);
            return "redirect:/admin/resource/adminApprove";
        }
        List<UserReceiverDto> allUnapprovedUsers = unApprovedUsersPageFromDb.stream().map(user -> new UserReceiverDto(user.getEmail(), user.getRole().toString())).toList();
        PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageNo);
        model.addAttribute("paginationReceiver",paginationReceiver);
        model.addAttribute("allUnapprovedUsers", allUnapprovedUsers);
        return "adminService/approveUsersGet";
    }

    @PostMapping("/adminApprove")
    public String adminApprove(@Valid @ModelAttribute EmailDto emailDto,BindingResult bindingResult,@RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/admin/resource/adminApprove",bindingResult);
        }
        //redirects to the pageNo(if possible) where user hit this request
        redirectAttributes.addAttribute("pageNo",pageNo);
        try {
            //attempts to approve a particular user based on their email
            boolean approved = tokenService.adminApproval(emailDto.getEmail());
            String msg = approved ? "Account has been approved" : "email cannot be approved";
            redirectAttributes.addFlashAttribute(approved ? "success" : "failure", msg);
            return "redirect:/admin/resource/adminApprove";
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("failure","Cannot approve user. Please try again");
            return "redirect:/admin/resource/adminApprove";
        }

    }
    @GetMapping("/adminDisprove")
    @Validated
    public String adminDisprove(@Positive(message = "Provide page no greater than zero") @RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo, Model model, RedirectAttributes redirectAttributes) {
try {
    // fetches all approved(adminApproved->true) users from db for possible blocking of user
    Page<User> approvedUsersPageFromDb = tokenService.getAllApprovedUsers(pageNo);
    int totalPages=approvedUsersPageFromDb.getTotalPages();
    //if user passes invalid pageNo(in this case ,it will always be greater than totalPages), redirect to last page.
    if(totalPages>0&& totalPages<pageNo){
        redirectAttributes.addAttribute("pageNo",totalPages);
        return "redirect:/admin/resource/adminDisprove";
    }
    PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageNo);
    model.addAttribute("approvedUsers", approvedUsersPageFromDb.toList());
    model.addAttribute("paginationReceiver",paginationReceiver);
    return "adminService/disapproveUserGet";
}
catch(Exception e){
    redirectAttributes.addFlashAttribute("failure","something went wrong");
    return "redirect:/admin/resource/adminApprove";
}
    }
    @PostMapping("/adminDisprove")
    public String adminDisprove(@Valid @ModelAttribute EmailDto emailDto,BindingResult bindingResult, @RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/admin/resource/adminDisprove",bindingResult);
        }
        //redirects to the pageNo(if possible) where user hit this request
        redirectAttributes.addAttribute("pageNo",pageNo);
        //attempts to set adminApproved->false in db to block user, based on their email
        try {
            boolean disproved = tokenService.disproveUser(emailDto.getEmail());
            String msg;
            if(disproved){
            /* when successfully set adminApproved->false,
              it invalidates all session associated with that user based on email*/
                invalidateSession.expireUserSessionsByEmail(emailDto.getEmail());
                msg="admin approval removed ";
            }
            else msg= "user does not exist";
            redirectAttributes.addFlashAttribute(disproved?"success":"failure",msg);
            return "redirect:/admin/resource/adminDisprove";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("error","Cannot disprove user. Please try again");
            return "redirect:/admin/resource/adminDisprove";
        }



    }
    @GetMapping("/dashboard")
    public String dashboardPage(){
        return "adminService/AdminDashboard";
    }


}

