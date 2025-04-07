package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherSearchCriteria;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO update(UUID voucherId, VoucherDTO voucherDTO);
    void delete(UUID voucherId);

    VoucherDTO order(UUID voucherId, UUID userId);
    VoucherDTO unorder(UUID voucherId);

    VoucherDTO changeHotStatus(UUID voucherId);

    VoucherDTO changeStatus(UUID voucherId, VoucherDTO voucherDTO);

    VoucherDTO getVoucherById(UUID voucherId);

    List<VoucherDTO> findAllByUserId(UUID userId);

    List<VoucherDTO> findAllByTourType(TourType tourType);
    List<VoucherDTO> findAllByTransferType(TransferType transferType);
    List<VoucherDTO> findAllByPrice(BigDecimal price);
    List<VoucherDTO> findAllByHotelType(HotelType hotelType);

    List<VoucherDTO> findAll();

    List<VoucherDTO> filterVouchers(VoucherSearchCriteria searchCriteria, boolean hotFirst);

}
