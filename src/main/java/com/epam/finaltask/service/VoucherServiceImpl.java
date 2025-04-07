package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherSearchCriteria;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.exception.VoucherNotFoundException;
import com.epam.finaltask.exception.VoucherNotOrderableException;
import com.epam.finaltask.exception.VoucherNotUnorderableException;
import com.epam.finaltask.exception.VoucherPriceInvalidException;
import com.epam.finaltask.exception.VoucherStatusInvalidException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.specification.VoucherSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService{

    private final VoucherRepository voucherRepository;

    private final MessageSource messageSource;

    private final VoucherMapper voucherMapper;
    private final UserMapper userMapper;

    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {

        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        voucher.setStatus(VoucherStatus.CREATED);

        if(voucherDTO.getUserId() == null){
            voucher.setUser(null); // otherwise hibernate tries to save user which is null
        }else{
            if(!userService.userExistsById(voucherDTO.getUserId())){
                log.warn("Voucher creating failed: user with id {} not found", voucherDTO.getUserId());
                throw new UserNotFoundException(messageSource.getMessage("user.not-found-id",
                        new Object[]{voucherDTO.getUserId()}, LocaleContextHolder.getLocale()));
            }
        }

        Voucher savedVoucher = voucherRepository.save(voucher);

        log.info("Voucher created with id {}", savedVoucher.getId());

        return voucherMapper.toVoucherDTO(savedVoucher);
    }

    @Override
    public VoucherDTO order(UUID voucherId, UUID userId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher ordering failed: Voucher with id {} not found", voucherId);
                    return new VoucherNotFoundException(messageSource.getMessage(
                            "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
                });

        if (voucher.getStatus() != VoucherStatus.CREATED) {
            log.warn("Voucher ordering failed: voucher with id {} cannot be ordered, current status: {}", voucherId, voucher.getStatus());
            throw new VoucherNotOrderableException(messageSource.getMessage(
                    "voucher.not-orderable.status", new Object[]{voucher.getStatus()}, LocaleContextHolder.getLocale()));
        }

        if (!userService.userExistsById(userId)) {
            log.warn("Voucher ordering failed: user with id {} not found", userId);
            throw new UserNotFoundException(messageSource.getMessage("user.not-found.id",
                    new Object[]{userId}, LocaleContextHolder.getLocale()));
        }

        voucher.setUser(userMapper.toUser(UserDTO.builder().id(userId).build()));
        voucher.setStatus(VoucherStatus.REGISTERED);

        log.info("Voucher with id {} ordered by user with id {}", voucherId, userId);

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO unorder(UUID voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher unordering failed: Voucher with id {} not found", voucherId);
                    return new VoucherNotFoundException(messageSource.getMessage(
                            "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
                });

        if (voucher.getStatus() != VoucherStatus.REGISTERED) {
            log.warn("Voucher unordering failed: voucher with id {} cannot be unordered, current status: {}",
                    voucherId, voucher.getStatus());
            throw new VoucherNotUnorderableException(messageSource.getMessage(
                    "voucher.not-unorderable.status", new Object[]{voucher.getStatus()}, LocaleContextHolder.getLocale()));
        }

        voucher.setUser(null);

        voucher.setStatus(VoucherStatus.CREATED);

        log.info("Voucher with id {} unordered", voucherId);

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO update(UUID voucherId, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher updating failed: voucher with id {} not found", voucherId);
                    return new VoucherNotFoundException(messageSource.getMessage(
                            "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
                });

        if (voucherDTO.getTitle() != null) {
            voucher.setTitle(voucherDTO.getTitle());
        }
        if (voucherDTO.getDescription() != null) {
            voucher.setDescription(voucherDTO.getDescription());
        }
        if (voucherDTO.getPrice() != null) {
            voucher.setPrice(voucherDTO.getPrice());
        }
        if (voucherDTO.getTourType() != null) {
            voucher.setTourType(TourType.valueOf(voucherDTO.getTourType()));
        }
        if (voucherDTO.getTransferType() != null) {
            voucher.setTransferType(TransferType.valueOf(voucherDTO.getTransferType()));
        }
        if (voucherDTO.getHotelType() != null) {
            voucher.setHotelType(HotelType.valueOf(voucherDTO.getHotelType()));
        }
        if (voucherDTO.getArrivalDate() != null) {
            voucher.setArrivalDate(voucherDTO.getArrivalDate());
        }
        if (voucherDTO.getEvictionDate() != null) {
            voucher.setEvictionDate(voucherDTO.getEvictionDate());
        }
        if(voucherDTO.getIsHot() != null){
            voucher.setHot(voucherDTO.getIsHot());
        }

        log.info("Voucher with id {} updated", voucherId);

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public void delete(UUID voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher deleting failed: voucher with id '{}' not found", voucherId);
                    return new VoucherNotFoundException(messageSource.getMessage(
                            "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
                });

        voucherRepository.delete(voucher);

        log.info("Voucher with id '{}' deleted", voucherId);
    }

    @Override
    public VoucherDTO changeHotStatus(UUID voucherId) {
        log.info("Changing hot status of voucher with id {}", voucherId);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher with id '{}' not found", voucherId);
                    return new VoucherNotFoundException(messageSource.getMessage(
                            "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
                });

        voucher.setHot(!voucher.isHot());
        Voucher updatedVoucher = voucherRepository.save(voucher);

        log.info("Hot status of voucher with id {} changed to {}", voucherId, voucher.isHot());

        return voucherMapper.toVoucherDTO(updatedVoucher);
    }

    @Override
    public VoucherDTO changeStatus(UUID voucherId, VoucherDTO voucherDTO) {
        log.info("Changing status of voucher with id {}", voucherId);

        Voucher voucher = findVoucherByIdOrThrow(voucherId);
        VoucherStatus newStatus = validateAndParseStatus(voucherId, voucherDTO);
        validatePrice(voucher);

        handlePaymentIfNeeded(voucher, newStatus);

        voucher.setStatus(newStatus);
        Voucher updatedVoucher = voucherRepository.save(voucher);

        log.info("Status of voucher with id {} changed to {}", voucherId, newStatus);
        return voucherMapper.toVoucherDTO(updatedVoucher);
    }

    @Override
    public VoucherDTO getVoucherById(UUID voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId).orElseThrow(() -> {
            log.warn("Voucher with id {} not found", voucherId);
            return new VoucherNotFoundException(messageSource.getMessage(
                    "voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale()));
        });

        log.info("Voucher with id {} found", voucherId);
        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public List<VoucherDTO> findAllByUserId(UUID userId) {
        log.info("Fetching vouchers for user with id {}", userId);

        if (!userService.userExistsById(userId)) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(messageSource.getMessage("user.not-found.id",
                    new Object[]{userId}, LocaleContextHolder.getLocale()));
        }

        List<Voucher> vouchers = voucherRepository.findAllByUserId(userId);
        log.info("Found {} vouchers for user with id '{}'", vouchers.size(), userId);
        return mapToDtoList(vouchers);
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        log.info("Fetching vouchers by tour type {}", tourType);
        return mapToDtoList(voucherRepository.findAllByTourType(tourType));
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(TransferType transferType) {
        log.info("Fetching vouchers by transfer type {}", transferType);
        return mapToDtoList(voucherRepository.findAllByTransferType(transferType));
    }

    @Override
    public List<VoucherDTO> findAllByPrice(BigDecimal price) {
        log.info("Fetching vouchers by price {}", price);
        return mapToDtoList(voucherRepository.findAllByPrice(price));
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        log.info("Fetching vouchers by hotel type {}", hotelType);
        return mapToDtoList(voucherRepository.findAllByHotelType(hotelType));
    }

    @Override
    public List<VoucherDTO> findAll() {
        log.info("Fetching all vouchers");
        return mapToDtoList(voucherRepository.findAll());
    }

    @Override
    public List<VoucherDTO> filterVouchers(VoucherSearchCriteria searchCriteria, boolean hotFirst) {
        log.info("Filtering vouchers with criteria: {}, hot first: {}", searchCriteria, hotFirst);

        Specification<Voucher> specification = Specification.where(VoucherSpecifications.hasTitle(searchCriteria.getTitle()))
                .and(VoucherSpecifications.hasDescription(searchCriteria.getDescription()))
                .and(VoucherSpecifications.hasTourType(searchCriteria.getTourType()))
                .and(VoucherSpecifications.hasTransferType(searchCriteria.getTransferType()))
                .and(VoucherSpecifications.hasHotelType(searchCriteria.getHotelType()))
                .and(VoucherSpecifications.hasStatus(searchCriteria.getStatus()))
                .and(VoucherSpecifications.hasPriceBetween(searchCriteria.getMinPrice(), searchCriteria.getMaxPrice()))
                .and(VoucherSpecifications.hasArrivalDateAfter(searchCriteria.getArrivalFrom()))
                .and(VoucherSpecifications.hasEvictionDateBefore(searchCriteria.getEvictionTo()));

        Sort sort;
        if (hotFirst){
            sort = Sort.by(Sort.Order.desc("isHot"));
        }else{
            sort = Sort.unsorted();
            specification = specification.and(VoucherSpecifications.hasHotStatus(searchCriteria.getHotStatus()));
        }

        List<Voucher> vouchers = voucherRepository.findAll(specification, sort);

        log.info("Filtered vouchers count: {}", vouchers.size());
        return mapToDtoList(vouchers);
    }

    private List<VoucherDTO> mapToDtoList(List<Voucher> vouchers) {
        return (vouchers == null || vouchers.isEmpty()) ? List.of() :
                vouchers.stream()
                        .map(voucherMapper::toVoucherDTO)
                        .toList();
    }

    private Voucher findVoucherByIdOrThrow(UUID voucherId) {
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> {
                    log.warn("Voucher with id {} not found", voucherId);
                    return new VoucherNotFoundException(
                            messageSource.getMessage("voucher.not-found.id", new Object[]{voucherId}, LocaleContextHolder.getLocale())
                    );
                });
    }

    private VoucherStatus validateAndParseStatus(UUID voucherId, VoucherDTO voucherDTO) {
        String statusStr = voucherDTO.getStatus();
        if (statusStr == null) {
            log.warn("Voucher status is null for voucher with id {}", voucherId);
            throw new VoucherStatusInvalidException(
                    messageSource.getMessage("voucher.status.null", null, LocaleContextHolder.getLocale())
            );
        }

        VoucherStatus newStatus = VoucherStatus.valueOf(statusStr);
        if (newStatus == VoucherStatus.CREATED || newStatus == VoucherStatus.REGISTERED) {
            log.warn("Invalid status change to {} for voucher with id {}", newStatus, voucherId);
            throw new VoucherStatusInvalidException(
                    messageSource.getMessage("voucher.status-change.invalid", new Object[]{newStatus}, LocaleContextHolder.getLocale())
            );
        }

        return newStatus;
    }

    private void validatePrice(Voucher voucher) {
        BigDecimal price = voucher.getPrice();
        UUID voucherId = voucher.getId();

        if (price == null) {
            log.warn("Voucher with id {} has null price", voucherId);
            throw new VoucherPriceInvalidException(
                    messageSource.getMessage("voucher.price.null", null, LocaleContextHolder.getLocale())
            );
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Voucher with id {} has negative price", voucherId);
            throw new VoucherPriceInvalidException(
                    messageSource.getMessage("voucher.price.negative", null, LocaleContextHolder.getLocale())
            );
        }
    }

    private void handlePaymentIfNeeded(Voucher voucher, VoucherStatus newStatus) {
        UUID userId = voucher.getUser().getId();
        BigDecimal price = voucher.getPrice();

        if (newStatus == VoucherStatus.PAID) {
            paymentService.processPayment(userId, price);
        } else if (newStatus == VoucherStatus.CANCELLED && voucher.getStatus() == VoucherStatus.PAID) {
            paymentService.refundPayment(userId, price);
        }
    }

}
