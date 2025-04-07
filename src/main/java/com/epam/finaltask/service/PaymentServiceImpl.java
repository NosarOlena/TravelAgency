package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.PaymentFailedException;
import com.epam.finaltask.exception.UserBalanceInvalid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService{

    private final UserService userService;
    private final MessageSource messageSource;

    @Transactional
    public void processPayment(UUID userId, BigDecimal amount) {
        validateAmount(amount);

        UserDTO user = userService.getUserById(userId);
        BigDecimal currentBalance = user.getBalance();

        if (currentBalance.compareTo(amount) < 0) {
            throw new PaymentFailedException(messageSource.getMessage(
                    "user.balance.insufficient-funds", null, LocaleContextHolder.getLocale()));
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setBalance(newBalance);
        userService.changeUserBalance(user);
    }

    @Transactional
    public void refundPayment(UUID userId, BigDecimal amount) {
        validateAmount(amount);

        UserDTO user = userService.getUserById(userId);

        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);
        userService.changeUserBalance(user);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new UserBalanceInvalid(messageSource.getMessage(
                    "user.balance.invalid-amount", null, LocaleContextHolder.getLocale()));
        }
    }

}
