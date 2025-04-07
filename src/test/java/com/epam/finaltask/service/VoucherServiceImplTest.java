package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private VoucherDTO voucherDTO;
    private Voucher voucher;
    private UserDTO userDTO;
    private User user;
    private UUID voucherId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        voucherId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userDTO = UserDTO.builder().id(userId).build();
        user = User.builder().id(userId).build();

        voucherDTO = new VoucherDTO();
        voucherDTO.setId(voucherId);
        voucherDTO.setTitle("Holiday Voucher");
        voucherDTO.setPrice(BigDecimal.valueOf(100));
        voucherDTO.setUserId(userId);
        voucherDTO.setStatus("CREATED");

        voucher = new Voucher();
        voucher.setId(voucherId);
        voucher.setTitle("Holiday Voucher");
        voucher.setPrice(BigDecimal.valueOf(100));
        voucher.setStatus(VoucherStatus.CREATED);
        voucher.setUser(user);
    }

    @Test
    void create_shouldCreateVoucherSuccessfully_whenUserExists() {
        when(userService.userExistsById(userId)).thenReturn(true);
        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucher);
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.create(voucherDTO);

        assertEquals(voucherDTO, result);
        verify(voucherRepository).save(voucher);
    }

    @Test
    void create_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucher);
        when(userService.userExistsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> voucherService.create(voucherDTO));
    }

    @Test
    void order_shouldOrderVoucherSuccessfully_whenVoucherExistsAndStatusIsCreated() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userService.userExistsById(userId)).thenReturn(true);
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);
        when(userMapper.toUser(userDTO)).thenReturn(user);

        VoucherDTO result = voucherService.order(voucherId, userId);

        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void order_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.order(voucherId, userId));
    }

    @Test
    void order_shouldThrowVoucherNotOrderableException_whenVoucherStatusIsNotCreated() {
        voucher.setStatus(VoucherStatus.REGISTERED);
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThrows(VoucherNotOrderableException.class, () -> voucherService.order(voucherId, userId));
    }

    @Test
    void order_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(userService.userExistsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> voucherService.order(voucherId, userId));
    }

    @Test
    void unorder_shouldUnorderVoucherSuccessfully_whenVoucherExistsAndStatusIsRegistered() {
        voucher.setStatus(VoucherStatus.REGISTERED);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.unorder(voucherId);

        assertEquals(voucherDTO, result);
        assertEquals(VoucherStatus.CREATED, voucher.getStatus());
        assertNull(voucher.getUser());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void unorder_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.unorder(voucherId));
    }

    @Test
    void unorder_shouldThrowVoucherNotUnorderableException_whenVoucherStatusIsNotRegistered() {
        voucher.setStatus(VoucherStatus.CREATED);
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThrows(VoucherNotUnorderableException.class, () -> voucherService.unorder(voucherId));
    }

    @Test
    void update_shouldUpdateVoucherSuccessfully_whenVoucherExists() {
        VoucherDTO updatedVoucherDTO = new VoucherDTO();
        updatedVoucherDTO.setTitle("Updated Holiday Voucher");
        updatedVoucherDTO.setPrice(BigDecimal.valueOf(150));

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(updatedVoucherDTO);

        VoucherDTO result = voucherService.update(voucherId, updatedVoucherDTO);

        assertEquals(updatedVoucherDTO.getTitle(), result.getTitle());
        assertEquals(updatedVoucherDTO.getPrice(), result.getPrice());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void update_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        VoucherDTO updatedVoucherDTO = new VoucherDTO();
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.update(voucherId, updatedVoucherDTO));
    }

    @Test
    void delete_shouldDeleteVoucherSuccessfully_whenVoucherExists() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        voucherService.delete(voucherId);

        verify(voucherRepository).delete(voucher);
    }

    @Test
    void delete_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.delete(voucherId));
    }

    @Test
    void changeHotStatus_shouldChangeVoucherHotStatusSuccessfully_whenVoucherExists() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.changeHotStatus(voucherId);

        assertNotNull(result);
        assertTrue(voucher.isHot());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void changeHotStatus_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.changeHotStatus(voucherId));
    }

    @Test
    void changeStatus_shouldChangeVoucherStatusSuccessfully_whenVoucherExists() {
        voucherDTO.setStatus("PAID");
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.changeStatus(voucherId, voucherDTO);

        assertEquals(VoucherStatus.PAID, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void changeStatus_shouldThrowVoucherStatusInvalidException_whenStatusIsNull() {
        voucherDTO.setStatus(null);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThrows(VoucherStatusInvalidException.class, () -> voucherService.changeStatus(voucherId, voucherDTO));
    }

    @Test
    void changeStatus_shouldThrowVoucherPriceInvalidException_whenPriceIsNegative() {
        voucher.setPrice(BigDecimal.valueOf(-100));
        voucherDTO.setStatus("PAID");
        voucher.setStatus(VoucherStatus.REGISTERED);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThrows(VoucherPriceInvalidException.class, () -> voucherService.changeStatus(voucherId, voucherDTO));
    }

    @Test
    void getVoucherById_shouldThrowVoucherNotFoundException_whenVoucherDoesNotExist() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        assertThrows(VoucherNotFoundException.class, () -> voucherService.getVoucherById(voucherId));
    }

    @Test
    void getVoucherById_shouldReturnVoucherSuccessfully_whenVoucherExists() {
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.getVoucherById(voucherId);

        assertEquals(voucherDTO, result);
    }
}
