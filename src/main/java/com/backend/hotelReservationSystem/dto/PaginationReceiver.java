package com.backend.hotelReservationSystem.dto;

import com.backend.hotelReservationSystem.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayDeque;

@AllArgsConstructor
@Getter
public class PaginationReceiver {
    private final Integer totalPages;
    private  final Integer pageNo;
    private static final int PAGE_DISPLAY=2;
    public static final int PAGE_SIZE=5;

    public ArrayDeque<Integer> indexes(){
        int count=pageNo;
        ArrayDeque<Integer> indexes= new ArrayDeque<>();
        boolean entered=false;
        for(int i=0;i<PAGE_DISPLAY&&i<totalPages;i++){
            if(isLast(count)){
                entered=true;
                indexes.add(count);
                count=pageNo-1;
                continue;
            }
            if (entered) {
                indexes.addFirst(count);
                count--;

            }
            else{
                indexes.add(count);
                count++;
            }

        }
        return indexes;
    }
    public boolean isFirst(int currentIndex){
        return currentIndex==1;
    }
    public boolean isLast(int currentIndex){
        return currentIndex==totalPages;
    }



}
