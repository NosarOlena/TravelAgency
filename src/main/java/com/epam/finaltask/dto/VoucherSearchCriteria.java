package com.epam.finaltask.dto;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class VoucherSearchCriteria {

    private UUID id;
    private String title;
    private String description;
    private TourType tourType;
    private TransferType transferType;
    private HotelType hotelType;
    private VoucherStatus status;
    private Boolean hotStatus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate arrivalFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate evictionTo;
}
