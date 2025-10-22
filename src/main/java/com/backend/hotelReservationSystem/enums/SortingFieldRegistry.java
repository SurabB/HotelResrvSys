package com.backend.hotelReservationSystem.enums;

import com.backend.hotelReservationSystem.dto.PageSortReceiver;
import com.backend.hotelReservationSystem.dto.PaginationReceiver;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public enum SortingFieldRegistry {
    BOOK_ROOM(Set.of("pricePerHour", "roomType")),
    CANCEL_BOOKING(Set.of("bookingDate", "checkInDate", "checkoutDate", "status", "paymentAmt")),
    CHANGE_ROOM_INFO(Set.of("roomNumber", "pricePerHour", "roomType", "roomIsActive")),
    VIEW_BOOKED_ROOMS(Set.of("bookingDate", "checkInDate", "checkoutDate", "paymentAmt", "pricePerHr", "status")),
    APPROVE_UNAPPROVE_USERS(Set.of("email", "role")),
    DISPLAY_BUSINESS(Set.of("businessName", "cityName", "location"));

    private final Set<String> allowedFields;

    SortingFieldRegistry(Set<String> allowedFields) {
        this.allowedFields = allowedFields;
    }

    public boolean isAllowed(String field) {
        return allowedFields.contains(field);
    }

    public Pageable getPageableObj(PageSortReceiver pageSortReceiver){
        String sortDir = pageSortReceiver.getSortDir();
        if(isAllowed(pageSortReceiver.getSortField())&&(
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
