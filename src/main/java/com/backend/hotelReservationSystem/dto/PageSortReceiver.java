package com.backend.hotelReservationSystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageSortReceiver {
    @Positive(message = "pageNo should be greater than zero")
    private Integer pageNo=1;

    @NotNull
    private String sortDir="none";

    @NotNull
    private String sortField="none";
}
