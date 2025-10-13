package com.backend.hotelReservationSystem.dto;

import com.backend.hotelReservationSystem.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayDeque;

@AllArgsConstructor
@Getter
public class PaginationReceiver {
    private final Page<Room> rooms;
    private  final Integer pageNo;
    private static final int PAGE_DISPLAY=3;

    public ArrayDeque<Integer> indexes(){
        int totalPages = rooms.getTotalPages();
        int count=pageNo;
        ArrayDeque<Integer> indexes= new ArrayDeque<>();
        boolean entered=false;
        for(int i=0;i<PAGE_DISPLAY&&i<totalPages;i++){
            if(isFirst(count)){
                entered=true;
                indexes.addFirst(count);
                count=pageNo+1;
                continue;
            }
            if (entered) {
                indexes.add(count);
                count++;

            }
            else{
                indexes.addFirst(count);
                count--;
            }

        }
        return indexes;
    }
    public boolean isFirst(int currentIndex){
        return currentIndex==1;
    }
    public boolean isLast(int currentIndex){
        return currentIndex==rooms.getTotalPages();
    }


}
