package com.epam.finaltask.exception;

import com.epam.finaltask.dto.LoginRequest;
import com.epam.finaltask.dto.UsernameUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFailedException.class)
    public String handleAuthenticationFailedException(AuthenticationFailedException ex, Model model) {
        log.warn("AuthenticationFailedException occurred");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/sign-in";
    }

    @ExceptionHandler(PaymentFailedException.class)
    public String handlePaymentFailedException(PaymentFailedException ex, RedirectAttributes redirectAttributes) {
        log.warn("AuthenticationFailedException occurred");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/api/manager/vouchers";
    }

    @ExceptionHandler(UserUsernameUnchangedException.class)
    public String handleUserUsernameUnchangedException(UserUsernameUnchangedException ex, Model model){
        log.info("UserUsernameUnchangedException occurred");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("usernameUpdateRequest", new UsernameUpdateRequest());
        return "user/update-username";
    }

    @ExceptionHandler({
            ConflictException.class,
            UserBalanceInvalid.class,
            UsernameTakenException.class,
            UserNotFoundException.class,
            UserStatusUnchangedException.class,
            VoucherNotFoundException.class,
            VoucherNotOrderableException.class,
            VoucherNotUnorderableException.class,
            VoucherPriceInvalidException.class,
            VoucherStatusInvalidException.class
    })
    public String handleCustomExceptions(Exception ex, Model model) {
        log.warn("Custom exception occurred: {}", ex.getClass().getSimpleName());

        String errorType = ex.getClass().getSimpleName();
        String errorMessage = ex.getMessage();

        model.addAttribute("errorType", errorType);
        model.addAttribute("errorMessage", errorMessage);

        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getClass().getSimpleName(), ex);
        return "error/internal-error";
    }

}
