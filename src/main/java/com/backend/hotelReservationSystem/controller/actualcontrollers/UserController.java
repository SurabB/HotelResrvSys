package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.userServiceDto.BookRoomDto;
import com.backend.hotelReservationSystem.dto.userServiceDto.CancelBookingDto;
import com.backend.hotelReservationSystem.dto.userServiceDto.RoomBook;
import com.backend.hotelReservationSystem.dto.userServiceDto.FindBusinessDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.*;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.service.actualservice.UserService;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/user/service")
@SessionAttributes("business")  // # business is a session attribute that must be available to perform user-business related opr.
//@PreAuthorize("hasRole('USER')")
public class UserController {
    private final UserRepo userRepo;
    private final UserService userService;

//  get all available(active) businesses for selection
    @GetMapping("/getBusiness")
    public String getBusiness(@SessionAttribute(value = "business",required = false)Business business, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {
        if(business!=null){
            sessionStatus.setComplete();
        }
        try {
            //gets all businesses which are active from db.
            List<Business> allAvailableBusinesses = userService.findAllAvailableBusinesses();
            model.addAttribute("allAvailableBusinesses", allAvailableBusinesses);
            return "userService/displayAllAvailableBusinesses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong on server side");
            return "redirect:/user/resource/home";
        }
    }

    //post request ,if succeed user selects a particular business as a service provider
    @PostMapping("/getBusiness")
    public String getBusiness(@Valid @ModelAttribute FindBusinessDto findbusinessdto, Model model, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            String firstError = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            redirectAttributes.addFlashAttribute("failure", firstError);
            return "redirect:/user/service/getBusiness";
        }

        // 1 findBusinessDto-> businessName,city,location (combined unique in db)
        // 2 find businesses having provided credentials.
        Optional<Business> business = userService.findBusiness(findbusinessdto);
        if(business.isEmpty()) {
            redirectAttributes.addFlashAttribute("failure", "Business does not exist");
            return "redirect:/user/service/getBusiness";
        }

        //3 creates or updates session attribute (business) (@SessionAttributes is present in class lvl )
        model.addAttribute("business",business.get());
        return "redirect:/user/service/businessPage";

    }
    @GetMapping("/businessPage")
    public String getBusinessPage(@SessionAttribute(value = "business",required = false) Business business, RedirectAttributes redirectAttributes) {
        System.out.println("/businessPage:"+business);
        if(business == null) {
            redirectAttributes.addFlashAttribute("failure", "Select a Business First");
            return "redirect:/user/service/getBusiness";
        }
        return "userService/businessUserServingPage";
    }

    @GetMapping("/getRoom")
    public String getRoom(@SessionAttribute(value = "business",required = false) Business business, RedirectAttributes redirectAttributes){
        if(business == null) {
            redirectAttributes.addFlashAttribute("failure", "Select a Business First");
            return "redirect:/user/service/getBusiness";
        }
        return "userService/bookRoom";
    }

// gets all available(active) rooms from selected business.
    @PostMapping("/getRoom")
    public String getRoom(@Valid @ModelAttribute BookRoomDto bookRoomDto,BindingResult bindingResult,@SessionAttribute(value = "business",required = false) Business business, Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/user/service/getRoom",bindingResult);
        }
        if(business==null) {

            //1 business is a session attribute that must be available to perform user-business related opr.
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
try {
    BookingPolicy.BookingTime time = BookingPolicy.getTime(bookRoomDto);
    //  2 find available(active) rooms for particular time duration from db using businessUuid stored in business(session attribute)
    List<Room> availableRoomsFromDb = userService.findAvailableRooms(business.getBusinessUuid(),time);


    Map<Room, BigDecimal> availableRoomsWithPrize = availableRoomsFromDb.stream().collect(Collectors.toMap(availableRoom -> availableRoom, availableRoom -> BookingPolicy.roomPrizeForDuration(time.getCheckInDate(), time.getCheckoutDate(), availableRoom.getPricePerHour()).orElseThrow(() -> new BookingCancellationException("Invalid Duration provided"))));
    redirectAttributes.addFlashAttribute("availableRooms", availableRoomsWithPrize);
    redirectAttributes.addFlashAttribute("checkInDate",time.getCheckInDate());
    redirectAttributes.addFlashAttribute("checkoutDate",time.getCheckoutDate());
    return "redirect:/user/service/bookRoom";

}
catch (BookingCancellationException ex){
    redirectAttributes.addFlashAttribute("failure", ex.getMessage());
    return "redirect:/user/service/getRoom";
}
catch (Exception e) {
    redirectAttributes.addFlashAttribute("failure", "something went wrong on server side");
    return "redirect:/user/service/getRoom";
}
    }
    @GetMapping("/bookRoom")
    public String bookRoom(@ModelAttribute(value = "availableRooms",binding = false) Map<Room,BigDecimal> availableRooms,
                           @ModelAttribute(value = "checkInDate",binding = false) LocalDateTime checkInDate,
                           @ModelAttribute(value = "checkoutDate",binding = false) LocalDateTime checkoutDate,
                           @SessionAttribute(value = "business",required = false)Business business,
                           RedirectAttributes redirectAttributes,Model model){
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        if(availableRooms==null||checkInDate==null||checkoutDate==null){
            redirectAttributes.addFlashAttribute("failure","First fill these credentials");
            return "redirect:/user/service/getRoom";
        }
        model.addAttribute("availableRooms",availableRooms);
        model.addAttribute("checkInDate",checkInDate);
        model.addAttribute("checkoutDate",checkoutDate);
        availableRooms.forEach((room, methodLocalDto) -> System.out.println("key(room):"+room+",value(methodlocal):"+methodLocalDto));
        return "userService/availableRooms";
    }

   // post request,if succeed user books room from a selected business
    @PostMapping("/bookRoom")
    public String bookRoom(@Valid @ModelAttribute("bookRoomDto") RoomBook roomBook, BindingResult bindingResult, @SessionAttribute(value = "business",required = false) Business business, Principal principal, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()){
           throw new CustomMethodArgFailedException("redirect:/user/service/getRoom",bindingResult);
        }
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }

        try {
            // 1 fetches current context user
            String username = principal.getName();

            Optional<User> userByEmail = userRepo.findUserByEmail(username);
            User user = userByEmail.orElseThrow(() -> new StaleUserException("user does not exist in db"));

            //book room dto post-> roomNumber(unique per business but not room),checkInTime,checkoutTime
            //business-> businessId(pk)
            //roomNumber+businessId-> uniquely identifies particular business room.

            // 2 books room based on above credentials
           userService.bookRoom(roomBook,user,business.getBusinessId());
            redirectAttributes.addFlashAttribute("success", "Room booked successfully");
            return "redirect:/user/service/getRoom";
        }
        catch (BookingCancellationException | RoomNotFoundException | InsufficientBalanceException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/user/service/getRoom";
        }
        catch(Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong on server");
            return "redirect:/user/service/getRoom";
        }

    }
    @GetMapping("/cancelBooking")
    public String cancelBooking(@SessionAttribute(value = "business",required = false) Business business,Model model,Principal principal,RedirectAttributes redirectAttributes) {
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        try {
            HashMap<ReservationTable, BigDecimal> bookingsAndRefundableAmt = userService.findBookingsOfParticularUser(principal.getName(), business.getBusinessId());
            model.addAttribute("bookingsAndRefundableAmt", bookingsAndRefundableAmt);
            return "userService/cancelBookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong on server");
            return "redirect:/user/service/businessPage";
        }
    }

    @PostMapping("/cancelBooking")
    public String cancelBooking(@Valid @ModelAttribute CancelBookingDto roomBookingCancel , RedirectAttributes redirectAttributes, Principal principal, @ModelAttribute("business") Business business) {
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure","Select Business to continue");
           return "redirect:/user/service/getBusiness";
        }
        try {
            userService.cancelBooking(roomBookingCancel, principal.getName(), business.getBusinessId());
            redirectAttributes.addFlashAttribute("success", "Booking was cancelled successfully");
            return "redirect:/user/service/cancelBooking";
        }
        catch(BookingCancellationException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/user/service/cancelBooking";
        }
        catch(Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong on server");
            return "redirect:/user/service/cancelBooking";
        }



    }

    //removes all attributes(mentioned in @sessionAttributes) from session if there is any.
    @GetMapping("/logout")
    public String logout(@SessionAttribute(value = "business",required = false) Business business, SessionStatus status,RedirectAttributes redirectAttributes) {
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        status.setComplete();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully");
        return "redirect:/user/service/getBusiness";
    }

}
