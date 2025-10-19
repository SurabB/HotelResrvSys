package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class SortingFields {
    public static final Set<String> BOOK_ROOM=Set.of("pricePerHour","roomType");
    public static final Set<String> CANCEL_BOOKING=Set.of("bookingDate","checkInDate","checkoutDate","status","paymentAmt");
    public static final Set<String> CHANGE_ROOM_INFO=Set.of("roomNumber","pricePerHour","roomType","roomIsActive");
    public static final Set<String> VIEW_BOOKED_ROOMS=Set.of("bookingDate","checkInDate","checkoutDate","paymentAmt","pricePerHr","status");
public  static final Set<String> Approve_UNAPPROVE_USERS =Set.of("email","role");
public  static final Set<String> DISPLAY_BUSINESS=Set.of("businessName","cityName","location");


    public static Pageable getPageableObj(PageSortReceiver pageSortReceiver,Set<String> sortingFieldHolder){
        String sortDir = pageSortReceiver.getSortDir();
        if(sortingFieldHolder.contains(pageSortReceiver.getSortField())&&(
                sortDir.equalsIgnoreCase("asc")||
                        sortDir.equalsIgnoreCase("desc")
        )
        ){
            Sort sort;
            if(sortDir.equalsIgnoreCase("asc")){
                sort=Sort.by(pageSortReceiver.getSortField()).ascending();
            }
            else  sort=Sort.by(pageSortReceiver.getSortField()).descending();

            return PageRequest.of(pageSortReceiver.getPageNo()-1, PaginationReceiver.PAGE_SIZE,sort);
        }
        else return PageRequest.of(pageSortReceiver.getPageNo()-1, PaginationReceiver.PAGE_SIZE);
    }
}
