package com.backend.hotelReservationSystem.comp;

import com.backend.hotelReservationSystem.dto.businessServiceDto.RoomUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, RoomUpdateDto> {

    @Override
    public boolean isValid(RoomUpdateDto form, ConstraintValidatorContext context) {
        if (form == null) {
            return false;
        }
        boolean roomIsActiveFilled = form.getRoomIsActive() != null;
        boolean pricePerHourFilled = form.getPricePerHour() != null;
        boolean roomTypeFilled = form.getRoomType() != null && !form.getRoomType().isBlank()&&form.getRoomType().length()<50;
        boolean imageFile = form.getMultipartFile() != null && !form.getMultipartFile().isEmpty();


        return roomIsActiveFilled || pricePerHourFilled || roomTypeFilled ||imageFile; // at least one must be filled
    }
}
