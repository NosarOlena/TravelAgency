package com.epam.finaltask.dto;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.validation.ValidDates;
import com.epam.finaltask.validation.ValidEnum;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ValidDates(message = "{voucher.dates.invalid}")
public class VoucherDTO {

    private UUID id;

    private String title;

    private String description;

    @PositiveOrZero(message = "{voucher.price.negative}")
    private BigDecimal price;

    @ValidEnum(enumClass = TourType.class, message = "{voucher.tour-type.invalid}")
    private String tourType;

    @ValidEnum(enumClass = TransferType.class, message = "{voucher.transfer-type.invalid}")
    private String transferType;

    @ValidEnum(enumClass = HotelType.class, message = "{voucher.hotel-type.invalid}")
    private String hotelType;

    @ValidEnum(enumClass = VoucherStatus.class, message = "{voucher.status.invalid}")
    private String status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    private UUID userId;

    private Boolean isHot;

}
