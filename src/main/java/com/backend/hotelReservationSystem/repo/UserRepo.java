package com.backend.hotelReservationSystem.repo;

import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User,Long> {
    @Modifying(clearAutomatically = true)
    @Query("Update User u set u.isAdminApproved =true where u.email = ?1 and u.isEmailVerified=true")
    int approveUserByEmail(String email);


    @Modifying(clearAutomatically = true)
    @Query("Update User u set u.isAdminApproved =false where u.email = ?1 and u.isEmailVerified=true and u.role!=?2")
    int removeAdminApproval(String email,Role role);


    @Query("select u from User u where u.email=?1")
    Optional<User> findUserByEmail(String email);


   @Query("select u from User u where u.isAdminApproved=false and u.isEmailVerified=true ")
   List<User> findUnapprovedUsers();


  @Query("select u from User u where u.isAdminApproved=true and u.role!=?1 and u.isEmailVerified=true")
    List<User> findApprovedUsers(Role role);
}
