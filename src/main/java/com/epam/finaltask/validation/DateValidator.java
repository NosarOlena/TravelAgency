package com.epam.finaltask.validation;

import com.epam.finaltask.dto.VoucherDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<ValidDates, VoucherDTO> {

    @Override
    public void initialize(ValidDates constraintAnnotation) {
    }

    @Override
    public boolean isValid(VoucherDTO voucherDTO, ConstraintValidatorContext context) {
        LocalDate arrivalDate = voucherDTO.getArrivalDate();
        LocalDate evictionDate = voucherDTO.getEvictionDate();
        if (arrivalDate == null || evictionDate == null) {
            return true;
        }

        return !evictionDate.isBefore(arrivalDate);
    }
}
