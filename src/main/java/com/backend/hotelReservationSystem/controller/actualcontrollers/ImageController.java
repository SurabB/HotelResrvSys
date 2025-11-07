package com.backend.hotelReservationSystem.controller.actualcontrollers;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.Image;
import com.backend.hotelReservationSystem.enums.SortingFieldRegistry;
import com.backend.hotelReservationSystem.exceptionClasses.RoomNotFoundException;
import com.backend.hotelReservationSystem.repo.BusinessRepo;
import com.backend.hotelReservationSystem.repo.RoomRepo;
import com.backend.hotelReservationSystem.utils.SomeHelpers;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/imageProcessor")
@PreAuthorize("hasRole('BUSINESS')")
public class ImageController {
    private final BusinessRepo businessRepo;
    private final RoomRepo roomRepo;


    @GetMapping("/uploadBusinessImage")
    public String uploadImage(){
        return "imageService/uploadBusinessImage";
    }

    @GetMapping("/uploadRoomImage")
    public String uploadRoomImage(){ return "imageService/uploadRoomImage"; }

    @PostMapping("/uploadBusinessImage")
    @Transactional
    @Validated
    public String uploadBusinessImage(@NotNull @RequestParam(name = "imageFile") MultipartFile multipartFile, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            if (multipartFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Please Provide image file");
                return "redirect:/imageProcessor/uploadBusinessImage";
            }
            Tika tika=new Tika();
            boolean imageTypeValid = SomeHelpers.isImageTypeValid(multipartFile, tika);
            if (!imageTypeValid) {
                redirectAttributes.addFlashAttribute("failure", "Please Provide image file with excepted type:png or jpeg");
                return "redirect:/imageProcessor/uploadBusinessImage";
            }

            Optional<Business> businessByEmail = businessRepo.findBusinessByEmail(principal.getName());
            Business foundBusiness = businessByEmail.orElseThrow(() -> new UsernameNotFoundException("Business not found"));
            foundBusiness.setImage(new Image(tika.detect(multipartFile.getInputStream()), multipartFile.getBytes()));
            businessRepo.save(foundBusiness);
            redirectAttributes.addFlashAttribute("success", "Image added successfully");
            return "redirect:/imageProcessor/uploadBusinessImage";

        }catch (UsernameNotFoundException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/imageProcessor/uploadBusinessImage";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong");
            return "redirect:/imageProcessor/uploadBusinessImage";
        }
    }
    @PostMapping("/uploadRoomImage")
    @Transactional
    @Validated
    public String uploadRoomImage(@NotNull @RequestParam(name = "imageFile") MultipartFile multipartFile, @NotNull(message = "Room not found") @RequestParam(value = "roomNo") Long roomNo, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            if (multipartFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Please Provide image file");
                return "redirect:/imageProcessor/uploadRoomImage";
            }
            Tika tika=new Tika();
            boolean imageTypeValid = SomeHelpers.isImageTypeValid(multipartFile, tika);
            if (!imageTypeValid) {
                redirectAttributes.addFlashAttribute("failure", "Please Provide image file with excepted type:png or jpeg");
                return "redirect:/imageProcessor/uploadRoomImage";
            }

            Optional<Room> roomFromDb = roomRepo.findParticularRoomByBusinessEmail(roomNo,principal.getName());
            Room room = roomFromDb.orElseThrow(() -> new RoomNotFoundException("Room not found"));
            room.setImage(new Image(tika.detect(multipartFile.getInputStream()), multipartFile.getBytes()));
            roomRepo.save(room);
            redirectAttributes.addFlashAttribute("success", "Room Image added successfully");
            return "redirect:/imageProcessor/uploadRoomImage";
        }
        catch (RoomNotFoundException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/imageProcessor/uploadBusinessImage";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong");
            return "redirect:/imageProcessor/uploadBusinessImage";
        }
    }
        @GetMapping("/businessImage")
        @PreAuthorize("hasAnyRole('USER','BUSINESS')")
        @ResponseBody
  public ResponseEntity<byte[]> displayBusinessImage( @RequestParam(value = "Uuid",required = false) String Uuid, @AuthenticationPrincipal UserDetails userDetails){
            Optional<Image> image;
            if(userDetails.getAuthorities().toString().equals("[ROLE_BUSINESS]")){
           image=businessRepo.findBusinessImageByEmail(userDetails.getUsername());
            }
            else {
                if(Uuid!=null)
                image = businessRepo.findBusinessImageByUuid(Uuid);
                else image=Optional.empty();
            }
            if(image.isEmpty()){

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }
            Image businessImage = image.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, businessImage.getImageType())
                   // .header(HttpHeaders.CACHE_CONTROL, "max-age=600")
                    .body(businessImage.getImage());


        }
        @PreAuthorize("hasAnyRole('USER','BUSINESS')")
        @GetMapping("/displayRoomImage")
        @Validated
    public ResponseEntity<byte[]> displayRoomImage(@RequestParam(value = "Uuid",required = false) String Uuid,@NotNull @RequestParam("roomNo") Long roomNo,@AuthenticationPrincipal UserDetails userDetails){
        Optional<Image> image ;
        if(userDetails.getAuthorities().toString().equals("[ROLE_BUSINESS]"))
        image= roomRepo.findRoomImageByBusinessEmail(userDetails.getUsername(),roomNo);
        else {
            if(Uuid!=null){
              image=  roomRepo.findRoomImageByBusinessUuid(Uuid,roomNo);
            }
            else image=Optional.empty();
        }
        if(image.isEmpty()){
            return ResponseEntity.ok()
                    .body(new byte[]{});
        }
        Image businessImage = image.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, businessImage.getImageType())
               // .header(HttpHeaders.CACHE_CONTROL, "max-age=600")
                .body(businessImage.getImage());


    }
    @PostMapping("/deleteBusinessImage")
    @Transactional
    public String deleteBusinessImage(Principal principal,RedirectAttributes redirectAttributes){
        try {
            Optional<Business> businessFromDb = businessRepo.findBusinessByEmail(principal.getName());
            Business business = businessFromDb.orElseThrow(() -> new UsernameNotFoundException("Business Not found"));
            business.setImage(null);
            redirectAttributes.addFlashAttribute("success", "Image removed successfully");
            return "redirect:/imageProcessor/uploadBusinessImage";
        }
        catch (UsernameNotFoundException e){
            redirectAttributes.addFlashAttribute("failure",e.getMessage());
            return "redirect:/imageProcessor/uploadBusinessImage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure","something went wrong");
            return "redirect:/imageProcessor/uploadBusinessImage";
        }



    }
    @GetMapping("/deleteRoomImages")
    public String deleteRoomImages(@Valid @ModelAttribute PageSortReceiver pageSortReceiver,Principal principal,Model model,RedirectAttributes redirectAttributes){
        Pageable pageableObj = SortingFieldRegistry.CHANGE_ROOM_INFO.getPageableObj(pageSortReceiver);
        Page<Room> roomByBusinessEmail = roomRepo.findRoomByBusinessEmail(principal.getName(), pageableObj);
        int totalPages = roomByBusinessEmail.getTotalPages();
        if(totalPages<pageSortReceiver.getPageNo()&&totalPages>0){
            redirectAttributes.addAttribute("pageNo",totalPages);
            return "redirect:/imageProcessor/deleteRoomImages";

        }
        PaginationReceiver paginationReceiver=new PaginationReceiver(totalPages,pageSortReceiver.getPageNo());
        model.addAttribute("rooms",roomByBusinessEmail.toList());
        model.addAttribute("paginationReceiver",paginationReceiver);
        return "businessService/deleteRoomImages";
    }
    @PostMapping("/deleteRoomImages")
    @Validated
    public String deleteRoomImages(@NotNull @RequestParam(value = "roomNo")Long roomNo,
                                   @RequestParam(value = "pageNo",defaultValue = "1")Integer pageNo,
                                   Principal principal, RedirectAttributes redirectAttributes, Model model){

        redirectAttributes.addAttribute("pageNo",pageNo);
        try {
            Optional<Room> particularRoomByBusinessEmail = roomRepo.findParticularRoomByBusinessEmail(roomNo, principal.getName());
            Room room = particularRoomByBusinessEmail.orElseThrow(() -> new RoomNotFoundException("Room not found"));
            room.setImage(null);
            roomRepo.save(room);
            redirectAttributes.addFlashAttribute("success", "Room Image removed successfully");
            return "redirect:/imageProcessor/deleteRoomImages";
        }
        catch (RoomNotFoundException e){
            redirectAttributes.addFlashAttribute("failure", e.getMessage());
            return "redirect:/imageProcessor/deleteRoomImages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "something went wrong");
            return "redirect:/imageProcessor/deleteRoomImages";
        }

    }


}
