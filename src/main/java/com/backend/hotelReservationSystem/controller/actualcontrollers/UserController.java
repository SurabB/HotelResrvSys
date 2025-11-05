package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import com.backend.hotelReservationSystem.dto.userServiceDto.*;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.*;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.service.actualservice.UserService;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import com.backend.hotelReservationSystem.utils.SomeHelpers;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/user/service")
@SessionAttributes("business")  // # business is a session attribute that must be available to perform user-business related opr.
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final UserRepo userRepo;
    private final UserService userService;

//  get all available(active) businesses for selection
    @GetMapping("/getBusiness")
    public String getBusiness(@Valid @ModelAttribute PageSortReceiver pageSortReceiver,BindingResult bindingResult,@SessionAttribute(value = "business",required = false)Business business, Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/user/resource/dashboard",bindingResult);
        }
        if(business!=null){
            redirectAttributes.addFlashAttribute("failure","You need to first logout from Business");
            return "redirect:/user/service/businessPage";
        }
        try {
            //gets all businesses which are active from db.
            Page<Business> allAvailableBusinesses = userService.findAllAvailableBusinesses(pageSortReceiver);
            int totalPages=allAvailableBusinesses.getTotalPages();
            //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
            if(totalPages>0&& totalPages<pageSortReceiver.getPageNo()){
                redirectAttributes.addAttribute("pageNo",totalPages);
                return "redirect:/user/service/getBusiness";
            }
            PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageSortReceiver.getPageNo());
            model.addAttribute("paginationReceiver",paginationReceiver);
            model.addAttribute("allAvailableBusinesses", allAvailableBusinesses.toList());
            return "userService/displayAllAvailableBusinesses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong on server side");
            return "redirect:/user/resource/home";
        }
    }

    //post request ,if succeed user selects a particular business as a service provider
    @PostMapping("/getBusiness")
    public String getBusiness(@Valid @ModelAttribute FindBusinessDto findbusinessdto, BindingResult bindingResult,@RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo, Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            String firstError = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            redirectAttributes.addFlashAttribute("failure", firstError);
            return "redirect:/user/service/getBusiness";
        }


        // 1 findBusinessDto-> businessName,city,location (combined unique in db)
        // 2 find businesses having provided credentials.
        Optional<Business> business = userService.findBusiness(findbusinessdto);
        if(business.isEmpty()) {
            redirectAttributes.addAttribute("pageNo",pageNo);
            redirectAttributes.addFlashAttribute("failure", "Business does not exist");
            return "redirect:/user/service/getBusiness";
        }

        //3 creates or updates session attribute (business) (@SessionAttributes is present in class lvl )
        model.addAttribute("business",business.get());
        return "redirect:/user/service/businessPage";

    }
    //provides business Page
    @GetMapping("/businessPage")
    public String getBusinessPage(@SessionAttribute(value = "business",required = false) Business business, RedirectAttributes redirectAttributes) {
        if(business == null) {
            redirectAttributes.addFlashAttribute("failure", "Select a Business First");
            return "redirect:/user/service/getBusiness";
        }
        return "userService/businessUserServingPage";
    }

    //provide view page for accepting booking credentials
    @GetMapping("/getRoom")
    public String getRoom(@SessionAttribute(value = "business",required = false) Business business, RedirectAttributes redirectAttributes){
        if(business == null) {
            redirectAttributes.addFlashAttribute("failure", "Select a Business First");
            return "redirect:/user/service/getBusiness";
        }
        return "userService/bookRoom";
    }

//accepts user input for booking room, parses time and redirect to /bookRoom
    @PostMapping("/getRoom")
    public String getRoom(@Valid @ModelAttribute BookRoomDto bookRoomDto, BindingResult bindingResult, @SessionAttribute(value = "business",required = false) Business business, HttpSession httpSession, RedirectAttributes redirectAttributes) {
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
    redirectAttributes.addFlashAttribute("time",time);
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
    //if succeed provides list of available rooms(potentially for booking)
    @GetMapping("/bookRoom")
    public String bookRoom(@Valid @ModelAttribute PageSortReceiver pageSortReceiver,
                           BindingResult bindingResult1,
                           @ModelAttribute(value = "time")BookingPolicy.BookingTime time,
                           BindingResult bindingResult2,
                           @SessionAttribute(value = "business",required = false)Business business,
                           Model model, RedirectAttributes redirectAttributes){
        if(bindingResult1.hasErrors()||bindingResult2.hasErrors()){
            redirectAttributes.addFlashAttribute("failure","Invalid credentials provided");
            return "redirect:/user/service/getRoom";
        }
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        if(time.getCheckInDate()==null||time.getCheckoutDate()==null){
            redirectAttributes.addFlashAttribute("failure","First fill this form");
            return "redirect:/user/service/getRoom";
        }
        Page<Room> availableRoomsFromDb = userService.findAvailableRooms(business.getBusinessUuid(),time,pageSortReceiver);
        int totalPages=availableRoomsFromDb.getTotalPages();
        //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
        if(totalPages>0&& totalPages<pageSortReceiver.getPageNo()){
            redirectAttributes.addFlashAttribute("time",time);
            redirectAttributes.addAttribute("pageNo",totalPages);
            return "redirect:/user/service/bookRoom";
        }
        Map<Room, BigDecimal> availableRoomsWithPrize = availableRoomsFromDb.stream().collect(Collectors.toMap(Function.identity(), availableRoom -> BookingPolicy.roomPrizeForDuration(time.getCheckInDate(), time.getCheckoutDate(), availableRoom.getPricePerHour()).orElseThrow(() -> new BookingCancellationException("Invalid Duration provided"))
                ,(existing,replacement)->existing,()->new LinkedHashMap<>()));
        PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageSortReceiver.getPageNo());
        model.addAttribute("availableRooms", availableRoomsWithPrize);
        model.addAttribute("checkInDate",time.getCheckInDate());
        model.addAttribute("checkoutDate",time.getCheckoutDate());
        model.addAttribute("paginationReceiver",paginationReceiver);
        return "userService/availableRooms";
    }

   // if succeed user books room from a selected business
    @PostMapping("/bookRoom")
    public String bookRoom(@Valid @ModelAttribute RoomBook roomBook,
                           BindingResult bindingResult, @SessionAttribute(value = "business",required = false) Business business,
                           @RequestParam(name = "pageNo",defaultValue = "1")Integer pageNo, Principal principal, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()){
           throw new CustomMethodArgFailedException("redirect:/user/service/getRoom",bindingResult);
        }
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        redirectAttributes.addAttribute("pageNo",pageNo);

        BookingPolicy.BookingTime time=new BookingPolicy.BookingTime(roomBook.getCheckInTime(),roomBook.getCheckoutTime());
        redirectAttributes.addFlashAttribute("time",time);
        try {
            // 1 fetches current context user
            String username = principal.getName();

            Optional<User> userByEmail = userRepo.findUserByEmail(username);
            User user = userByEmail.orElseThrow(() -> new StaleUserException("user does not exist in db"));

            //book room-> roomNumber(unique per business but not room),checkInDate,checkoutTime
            //business-> businessId(pk)
            //roomNumber+businessId-> uniquely identifies particular business room.

            // 2 books room based on above credentials
           userService.bookRoom(roomBook,user,business.getBusinessId());
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
    //fetches particular user booking and send it to the user
    @GetMapping("/cancelBooking")
    public String cancelBooking(@Valid @ModelAttribute PageSortReceiver pageSortReceiver,BindingResult bindingResult,@SessionAttribute(value = "business",required = false) Business business,Model model,Principal principal,RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("failure","Invalid credentials Provided");
            return "redirect:/user/service/businessPage";
        }
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        try {
            Page<ReservationTable> bookingsAndRefundableAmtpage = userService.findBookingsOfParticularUser(principal.getName(), business.getBusinessId(),pageSortReceiver);
            int totalPages=bookingsAndRefundableAmtpage.getTotalPages();
            //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
            if(totalPages>0&& totalPages<pageSortReceiver.getPageNo()){
                redirectAttributes.addAttribute("pageNo",totalPages);
                return "redirect:/user/service/cancelBooking";
            }
            Map<ReservationTable, BigDecimal> bookingsAndRefundableAmt = SomeHelpers.convertToMap(bookingsAndRefundableAmtpage.toList());
            PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageSortReceiver.getPageNo());
            model.addAttribute("bookingsAndRefundableAmt", bookingsAndRefundableAmt);
            model.addAttribute("paginationReceiver",paginationReceiver);
            return "userService/cancelBookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong on server");
            return "redirect:/user/service/businessPage";
        }
    }

    //if succeeded, user cancels the particular room booking
    @PostMapping("/cancelBooking")
    public String cancelBooking(@Valid @ModelAttribute CancelBookingDto roomBookingCancel ,
                                BindingResult bindingResult, @RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo,
                                RedirectAttributes redirectAttributes, Principal principal, @SessionAttribute(value = "business",required = false) Business business) {
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/user/service/cancelBooking",bindingResult);
        }
        if(business==null) {
            redirectAttributes.addFlashAttribute("failure","Select Business to continue");
           return "redirect:/user/service/getBusiness";
        }
        redirectAttributes.addAttribute("pageNo",pageNo);
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
    @PostMapping("/earlyCheckout")
    public String earlyCheckout(@Valid @ModelAttribute CancelBookingDto earlyCheckout ,BindingResult bindingResult,
                                @RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo,
                                RedirectAttributes redirectAttributes, Principal principal, @SessionAttribute(value = "business",required = false) Business business) {
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/user/service/earlyCheckout",bindingResult);
        }
        if (business == null) {
            redirectAttributes.addFlashAttribute("failure", "Select Business to continue");
            return "redirect:/user/service/getBusiness";
        }
        redirectAttributes.addAttribute("pageNo",pageNo);
        try {
            boolean succeed = userService.earlyCheckout(earlyCheckout, principal.getName(), business.getBusinessId());
            if (succeed) {
                redirectAttributes.addFlashAttribute("success", "Checkout done");
                return "redirect:/user/service/cancelBooking";
            }
            redirectAttributes.addFlashAttribute("failure", "Cannot checkout until checkedIn");
            return "redirect:/user/service/cancelBooking";
        }
        catch(BookingCancellationException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/user/service/cancelBooking";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Something went wrong");
            return "redirect:/user/service/cancelBooking";
        }
    }
    //removes all attributes(mentioned in @sessionAttributes) from session if there is any.
    @PostMapping("/logout")
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
