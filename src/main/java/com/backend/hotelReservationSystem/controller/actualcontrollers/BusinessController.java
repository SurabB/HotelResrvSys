package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import com.backend.hotelReservationSystem.dto.businessServiceDto.BusinessRegAcceptor;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomAcceptorDto;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomUpdateDto;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.service.actualservice.BusinessService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
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
import java.util.function.Function;
import java.util.stream.Collectors;


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

    @GetMapping("/changeRoomInfo")
    public String changeRoomInfo(@RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo,RedirectAttributes redirectAttributes, Principal principal, Model model){
        Page<Room> allRooms = businessService.findRoomByBusinessEmail(principal.getName(),pageNo);
        int totalPages=allRooms.getTotalPages();
        //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
        if(totalPages>0&& totalPages<pageNo){
            redirectAttributes.addAttribute("pageNo",totalPages);
            return "redirect:/business/service/changeRoomInfo";
        }
        PaginationReceiver paginationReceiver = new PaginationReceiver(totalPages, pageNo);
        model.addAttribute("paginationReceiver",paginationReceiver);
        model.addAttribute("allRooms",allRooms.toList());
        return "businessService/changeRoomInfo";
    }
    @PostMapping("/changeRoomInfo")
    public  String changeRoomInfo(@Valid @ModelAttribute RoomUpdateDto roomUpdateDto, BindingResult bindingResult,
                                  @RequestParam(value = "pageNo",defaultValue = "1")Integer pageNo,
                                  Principal principal, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            throw new CustomMethodArgFailedException("redirect:/business/service/changeRoomInfo",bindingResult);
        }
        redirectAttributes.addAttribute("pageNo",pageNo);
        try {
            boolean changed = businessService.changeRoomInfo(roomUpdateDto, principal.getName());
            if (changed) {
                redirectAttributes.addFlashAttribute("success", "Room info changed successfully!");
                return "redirect:/business/service/changeRoomInfo";
            }
            redirectAttributes.addFlashAttribute("failure", "invalid credentials.");
            return "redirect:/business/service/changeRoomInfo";
        }
        catch (Exception ex) {
            redirectAttributes.addFlashAttribute("failure", "Something went wrong on server side.");
            return "redirect:/business/service/changeRoomInfo";
        }

    }
    @GetMapping("/findBookedRooms")
    public String findBookedRooms(@RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal, Model model){
        Page<ReservationTable> bookedRoomsPage = businessService.findBookedRooms(principal.getName(),pageNo);
        int totalPages=bookedRoomsPage.getTotalPages();
        //if user passes invalid pageNo(in this case ,it will always be greater than totalPages) ,redirect to last page.
        if(totalPages>0&& totalPages<pageNo){
            redirectAttributes.addAttribute("pageNo",totalPages);
            return "redirect:/business/service/findBookedRooms";
        }
        Map<ReservationTable,Room >bookedRooms = bookedRoomsPage.stream().collect(Collectors.toMap(Function.identity(), reservationTable -> reservationTable.getRoom()));
        PaginationReceiver paginationReceiver = new PaginationReceiver(bookedRoomsPage.getTotalPages(), pageNo);
        model.addAttribute("paginationReceiver",paginationReceiver);
        model.addAttribute("bookedRooms",bookedRooms);
        return  "businessService/bookedRooms";
    }




}
