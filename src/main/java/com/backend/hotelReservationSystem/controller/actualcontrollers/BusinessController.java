package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.businessServiceDto.BusinessRegAcceptor;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomAcceptorDto;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomUpdateDto;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.service.actualservice.BusinessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@Controller
@AllArgsConstructor
@RequestMapping("/business/service")
@PreAuthorize("hasRole('BUSINESS')&& @customAuth.isBusinessReg()")// custom auth mechanism
public class BusinessController {
    private final BusinessService businessService;
    @GetMapping("/businessReg")
    @PreAuthorize("hasRole('BUSINESS')&& (!@customAuth.isBusinessReg())")
    public String businessReg() {
        return "businessService/businessReg";

    }

    @PostMapping("/businessReg")
    @PreAuthorize("hasRole('BUSINESS')&& (!@customAuth.isBusinessReg())")
    public String businessReg(@Valid @ModelAttribute BusinessRegAcceptor businessRegAcceptor, BindingResult bindingResult,
                              Principal principal, RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/business/service/businessReg",bindingResult);
        }
        try {
                businessService.addBusiness(businessRegAcceptor,principal.getName());
                redirectAttributes.addFlashAttribute("success", "Business added successfully!");
                return "redirect:/business/resource/dashboard";
        }
        catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("failure", "Business already exists!");
            return "redirect:/business/service/businessReg";
        }
        catch (Exception ex) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong on server side.");
            return "redirect:/business/service/businessReg";


        }
    }
    @GetMapping("/addRoom")
    public String addRoom(Model model){
        model.addAttribute("roomAcceptor", new RoomAcceptorDto());

        return "businessService/addRoom";
    }
    @PostMapping("/addRoom")
    public String addRoom(@Valid @ModelAttribute RoomAcceptorDto roomAcceptorDto, BindingResult bindingResult,
                          Principal principal, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/business/service/addRoom",bindingResult);
        }
        try {
              businessService.addRoom(roomAcceptorDto,principal.getName(),redirectAttributes);
            redirectAttributes.addFlashAttribute("success", "Room added successfully!");
              return "redirect:/business/service/addRoom";

        }
        catch (DataIntegrityViolationException e){
            redirectAttributes.addFlashAttribute("failure", "Room number already exists. select Different Room number!");
            return "redirect:/business/service/addRoom";
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
                redirectAttributes.addFlashAttribute("failure", "Something went wrong on server side.");
                return "redirect:/business/service/addRoom";
        }

    }

    @GetMapping("/changeRoomStatus")
    public String changeRoomStatus(Principal principal, Model model){
        List<Room> allRooms = businessService.getAllRooms(principal.getName());
        model.addAttribute("allRooms",allRooms);
        return "businessService/changeRoomStatus";

    }

    @PostMapping("/changeRoomStatus")
    @Validated
    public String changeRoomStatus(@NotNull(message = "room Number should not be blank") @RequestParam("roomNumber") Long roomNumber, Principal principal, RedirectAttributes redirectAttributes){
        try {

            businessService.changeStatusOfRoom(principal.getName(),roomNumber);
                redirectAttributes.addFlashAttribute("success", "Room status changed successfully!");
                return "redirect:/business/service/changeRoomStatus";
        }
        catch (Exception ex) {
            redirectAttributes.addFlashAttribute("failure", "Something went wrong on server side.");
            return "redirect:/business/service/changeRoomStatus";
        }

    }
    @GetMapping("/changeRoomInfo")
    public String changeRoomInfo(Principal principal, Model model){
        List<Room> allAvailableRooms = businessService.getAllAvailableRooms(principal.getName());
        model.addAttribute("allAvailableRooms",allAvailableRooms);
        return "businessService/changeRoomInfo";
    }
    @PostMapping("/changeRoomInfo")
    public  String changeRoomInfo(@Valid @ModelAttribute RoomUpdateDto roomUpdateDto, BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/business/service/changeRoomInfo",bindingResult);
        }
        try {
            boolean changed = businessService.changeRoomInfo(roomUpdateDto, principal.getName());
            if (changed) {
                redirectAttributes.addFlashAttribute("success", "Room info changed successfully!");
                return "redirect:/business/service/changeRoomInfo";
            }
            redirectAttributes.addFlashAttribute("failure", "invalid credentials.");
            return "redirect:/business/service/changeRoomInfo";
        }
        catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("failure", "Room number already exist for that room. provide different roomNumber.");
            return "redirect:/business/service/changeRoomInfo";
        }
        catch (Exception ex) {
            redirectAttributes.addFlashAttribute("failure", "Something went wrong on server side.");
            return "redirect:/business/service/changeRoomInfo";
        }

    }
    @GetMapping("/findBookedRooms")
    public String findBookedRooms(Principal principal, Model model){
        Map<Room, ReservationTable> bookedRooms = businessService.findBookedRooms(principal.getName());
        model.addAttribute("bookedRooms",bookedRooms);
        return  "businessService/bookedRooms";
    }




}
