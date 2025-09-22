package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.userServiceDto.BookRoomDto;
import com.backend.hotelReservationSystem.dto.userServiceDto.RoomBookAndCancel;
import com.backend.hotelReservationSystem.dto.userServiceDto.FindBusinessDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.*;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.service.actualservice.UserService;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

// gets all available(active) rooms from selected business.
    @GetMapping("/bookRoom")
    public String bookRoom(@Valid @ModelAttribute BookRoomDto bookRoomDto,@SessionAttribute(value = "business",required = false) Business business, Model model, RedirectAttributes redirectAttributes) {
        if(business==null) {

            //1 business is a session attribute that must be available to perform user-business related opr.
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
try {
    BookingPolicy.BookingTime time = BookingPolicy.getTime(bookRoomDto);
    //  2 find available(active) rooms for particular time duration from db using businessUuid stored in business(session attribute)
    List<Room> availableRoomsFromDb = userService.findAvailableRooms(business.getBusinessUuid(),time);

    @AllArgsConstructor
    @Getter
    class MethodLocalDto{
        private final LocalDateTime startingTime;
       private final LocalDateTime endingTime;
       private final BigDecimal totalPrice;
    }
    Map<Room, MethodLocalDto> availableRooms = availableRoomsFromDb.stream().collect(Collectors.toMap(availableRoom -> availableRoom,availableRoom -> new MethodLocalDto(time.getCheckInDate(),time.getCheckoutDate(),BookingPolicy.roomPrizeForDuration(time.getCheckInDate(), time.getCheckoutDate(), availableRoom.getPricePerHour()).orElseThrow(()->new BookingCancellationException("Invalid Duration provided")))));
    model.addAttribute("availableRooms", availableRooms);
    return "userService/availableRooms";
} catch (Exception e) {
    redirectAttributes.addFlashAttribute("failure", "something went wrong on server side");
    return "redirect:/user/service/businessPage";
}
    }

   // post request,if succeed user books room from a selected business
    @PostMapping("/bookRoom")
    public String bookRoom(@Valid @ModelAttribute("bookRoomDto") RoomBookAndCancel roomBookAndCancel, BindingResult bindingResult, @SessionAttribute(value = "business",required = false) Business business, Principal principal, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()){
           throw new CustomMethodArgFailedException("redirect:/user/service/bookRoom",bindingResult);
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
           userService.bookRoom(roomBookAndCancel,user,business.getBusinessId());
            redirectAttributes.addFlashAttribute("success", "Room booked successfully");
            return "redirect:/user/service/bookRoom";
        }
        catch (BookingCancellationException | RoomNotFoundException | InsufficientBalanceException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/user/service/bookRoom";
        }
        catch(Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong on server");
            return "redirect:/user/service/bookRoom";
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
    public String cancelBooking(@Valid @ModelAttribute RoomBookAndCancel roomBookingCancel , RedirectAttributes redirectAttributes, Principal principal, @ModelAttribute("business") Business business) {
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
