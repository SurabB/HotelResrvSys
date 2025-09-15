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
        boolean roomNumberFilled = form.getRoomNumber() != null;
        boolean pricePerHourFilled = form.getPricePerHour() != null;
        boolean roomTypeFilled = form.getRoomType() != null && !form.getRoomType().isBlank()&&form.getRoomType().length()<50;

        return roomNumberFilled || pricePerHourFilled || roomTypeFilled; // at least one must be filled
    }
}
