package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.businessServiceDto.BusinessRegAcceptor;
import com.backend.hotelReservationSystem.entity.embeddable.Image;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomAcceptorDto;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomUpdateDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.BookingCancellationException;
import com.backend.hotelReservationSystem.exceptionClasses.StaleUserException;
import com.backend.hotelReservationSystem.repo.BusinessRepo;
import com.backend.hotelReservationSystem.repo.ReservationRepo;
import com.backend.hotelReservationSystem.repo.RoomRepo;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import com.backend.hotelReservationSystem.enums.SortingFieldRegistry;
import com.backend.hotelReservationSystem.utils.SomeHelpers;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.IllegalFormatFlagsException;
import java.util.Optional;


@Service
@AllArgsConstructor
@Transactional
public class BusinessService {
   private final BusinessRepo businessRepo;
   private final RoomRepo roomRepo;
   private final UserRepo userRepo;
   private final ReservationRepo reservationRepo;
   // if request succeeds, business get added to db
   public void addBusiness(BusinessRegAcceptor businessRegAcceptor,String username){

       /* if not found it means business is removed from db and current
          user is actually using stale session to perform opr */
       User user= userRepo.findUserByEmail(username)
               .orElseThrow(()->new StaleUserException("user :"+username+"is a stale user"));

       /* StaleUserException -> when this is caught in global exceptional handler,
          it will expire all sessions of current user by their username(email)*/

     //  CustomBuilder-> a static class that maps dto's ---> entity
       Business regBusiness = CustomBuilder.buildBusiness(businessRegAcceptor,user);
       if(user.getBusiness()!=null){
           throw new DataIntegrityViolationException("Business already exists");
       }

           businessRepo.save(regBusiness);
   }
   public void addRoom(RoomAcceptorDto roomAcceptorDto, String businessEmail, RedirectAttributes redirectAttributes){
       User user = userRepo.findUserByEmail(businessEmail)
               .orElseThrow(()->new StaleUserException("user :"+businessEmail+"is a stale user"));
       Room room = CustomBuilder.buildRoom(roomAcceptorDto,user.getBusiness());
       boolean roomExists = roomRepo.existRoomByRoomNumberAndBusinessEmail(room.getRoomNumber(), businessEmail);

       if(roomExists){
           throw new DataIntegrityViolationException("Room number already exists");

       }
           roomRepo.save(room);

       }

    public Page<Room> findRoomByBusinessEmail(String userEmail, PageSortReceiver pageSortReceiver) {
         Pageable pageRequest = SortingFieldRegistry.CHANGE_ROOM_INFO.getPageableObj(pageSortReceiver);
        return roomRepo.findRoomByBusinessEmail(userEmail, ReservationStatus.BOOKED,pageRequest);
    }

    public boolean changeRoomInfo(RoomUpdateDto roomUpdateDto, String businessEmail) throws IOException {
//   check if there is a room that has room number associated with business that user wants to change.
        Optional<Room> particularRoomByBusinessEmail = roomRepo.findParticularRoomByBusinessEmail(roomUpdateDto.getActiveRoomNumber(), businessEmail);

        //if no room exist returns false->room has been removed or user passed inaccurate room number
      if(particularRoomByBusinessEmail.isEmpty()){
          return false;
      }

      //if exist get the room
        Room room = particularRoomByBusinessEmail.get();

      //get the room status that the user wants to change to.
        Boolean userProvidedStatus = roomUpdateDto.getRoomIsActive();

        if(userProvidedStatus!= null){
            room.setRoomIsActive(!userProvidedStatus);
      }

        //null and empty check for optional field
        String userProvidedRoomType = roomUpdateDto.getRoomType();
        if(userProvidedRoomType!=null&&!userProvidedRoomType.isBlank()){
            room.setRoomType(userProvidedRoomType);

        }
        //null and less than 0 check for optional field
        BigDecimal UserProvidePricePerHour = roomUpdateDto.getPricePerHour();
        if(UserProvidePricePerHour!=null &&UserProvidePricePerHour.compareTo(BigDecimal.ZERO)>0){
            room.setPricePerHour(UserProvidePricePerHour);
        }
        MultipartFile imageFile = roomUpdateDto.getMultipartFile();
        if(imageFile!=null&& !imageFile.isEmpty()){
            Tika tika=new Tika();
            boolean imageTypeValid = SomeHelpers.isImageTypeValid(imageFile,tika);
            if(imageTypeValid) {
                room.setRoomImage(new Image(tika.detect(imageFile.getInputStream()),imageFile.getBytes()));
            }
            else {
                throw new MultipartStream.MalformedStreamException("Provided file format does not match expected file format.Allowed format: image/");
            }
        }
      roomRepo.save(room);
      return true;
    }

    public Page<ReservationTable> findBookedRooms(String businessEmail, PageSortReceiver pageSortReceiver) {
        Pageable  pageRequest = SortingFieldRegistry.VIEW_BOOKED_ROOMS.getPageableObj(pageSortReceiver);
        return reservationRepo.findBookedReservationAndRooms(businessEmail, ReservationStatus.BOOKED,ReservationStatus.CHECKED_IN,LocalDateTime.now(),pageRequest);




    }
}
