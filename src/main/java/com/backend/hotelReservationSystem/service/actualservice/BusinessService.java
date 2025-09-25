package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.dto.businessServiceDto.BusinessRegAcceptor;
import com.backend.hotelReservationSystem.enums.ReservationStatus;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomAcceptorDto;
import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomUpdateDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.entity.ReservationTable;
import com.backend.hotelReservationSystem.entity.Room;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.StaleUserException;
import com.backend.hotelReservationSystem.repo.BusinessRepo;
import com.backend.hotelReservationSystem.repo.ReservationRepo;
import com.backend.hotelReservationSystem.repo.RoomRepo;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.utils.CustomBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class BusinessService {
   private final BusinessRepo businessRepo;
   private final RoomRepo roomRepo;
   private final UserRepo userRepo;
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







    public void changeStatusOfRoom(String businessEmail,Long roomNumber) {

        int changedStatus = roomRepo.changeActiveStatus(businessEmail,roomNumber);
        if (changedStatus==0){
            throw  new StaleUserException("user :"+businessEmail+"is a stale user");
        }

    }

    public List<Room> getAllRooms(String userEmail) {
       return roomRepo.getAllRooms(userEmail);

    }

    public List<Room> getAllAvailableRooms(String userEmail) {
     return roomRepo.findAvailableRoomsByEmail(userEmail, ReservationStatus.BOOKED);
    }

    public boolean changeRoomInfo(RoomUpdateDto roomUpdateDto, String businessEmail) {
//   check if there is a room that has room number associated with business that user wants to change.
        Optional<Room> particularRoomByBusinessEmail = roomRepo.findParticularRoomByBusinessEmail(roomUpdateDto.getActiveRoomNumber(), businessEmail, ReservationStatus.BOOKED);

        //if no room exist returns false->room has been removed or user passed inaccurate room number
      if(particularRoomByBusinessEmail.isEmpty()){
          return false;
      }

      //if exist get the room
        Room room = particularRoomByBusinessEmail.get();

      //get the room number that the user wants to change to.
        Long userProvideNumber = roomUpdateDto.getRoomNumber();

        if(userProvideNumber!= null&&!userProvideNumber.equals(room.getRoomNumber())&&userProvideNumber>0){
            room.setRoomNumber(userProvideNumber);
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
      roomRepo.save(room);
      return true;
    }

    public Map<Room,ReservationTable> findBookedRooms(String businessEmail) {
        List<Room> bookedReservationAndRooms = roomRepo.findBookedReservationAndRooms(businessEmail, ReservationStatus.BOOKED);
         return bookedReservationAndRooms.stream().collect(Collectors.toMap(room -> room, room -> room.getReservation().getFirst()));


    }
}
