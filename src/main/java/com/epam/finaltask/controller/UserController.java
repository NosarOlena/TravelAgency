package com.epam.finaltask.controller;

import com.epam.finaltask.dto.PhoneUpdateRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.UserProfileDTO;
import com.epam.finaltask.dto.UsernameUpdateRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherSearchCriteria;
import com.epam.finaltask.exception.UsernameTakenException;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.auth.AuthenticationService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final VoucherService voucherService;

    private final MessageSource messageSource;

    @GetMapping("/dashboard")
    public String showDashboardPage(@RequestParam(required = false) String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String tourType,
                                    @RequestParam(required = false) String transferType,
                                    @RequestParam(required = false) String hotelType,
                                    @RequestParam(required = false) LocalDate arrivalFrom,
                                    @RequestParam(required = false) LocalDate evictionTo,
                                    @RequestParam(required = false) BigDecimal minPrice,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    Model model) {

        TourType tourTypeEnum = (tourType != null && !tourType.isBlank()) ? TourType.valueOf(tourType) : null;
        TransferType transferTypeEnum = (transferType != null && !transferType.isBlank()) ? TransferType.valueOf(transferType) : null;
        HotelType hotelTypeEnum = (hotelType != null && !hotelType.isBlank()) ? HotelType.valueOf(hotelType) : null;

        VoucherSearchCriteria criteria = VoucherSearchCriteria.builder()
                .title(title)
                .description(description)
                .tourType(tourTypeEnum)
                .transferType(transferTypeEnum)
                .hotelType(hotelTypeEnum)
                .status(VoucherStatus.CREATED)
                .arrivalFrom(arrivalFrom)
                .evictionTo(evictionTo)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        List<VoucherDTO> vouchers = voucherService.filterVouchers(criteria, true);

        model.addAttribute("vouchers", vouchers);

        return "user/user-dashboard";
    }


    @PostMapping("/order/{voucherId}")
    public String orderVoucher(@AuthenticationPrincipal UserDetails currentUser,
                               @PathVariable @NotNull UUID voucherId,
                               RedirectAttributes redirectAttributes) {
        UUID userId = userService.getUserByUsername(currentUser.getUsername()).getId();
        voucherService.order(voucherId, userId);

        String successMessage = messageSource.getMessage("voucher.order.success",
                null, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("successMessage", successMessage);

        return "redirect:/api/user/dashboard";
    }

    @GetMapping("/me")
    public String showMePage(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        UserDTO user = userService.getUserByUsername(currentUser.getUsername());

        UserProfileDTO userProfile = UserProfileDTO.builder()
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .balance(user.getBalance())
                .build();

        model.addAttribute("userProfile", userProfile);
        return "user/profile";
    }

    @GetMapping("/me/update-username")
    public String showUpdateUsernamePage(Model model){
        model.addAttribute("usernameUpdateRequest", new UsernameUpdateRequest());
        return "user/update-username";
    }

    @PostMapping("/me/update-username")
    public String updateUsername(
            @AuthenticationPrincipal UserDetails currentUser,
            @ModelAttribute("usernameUpdateRequest") @NotNull @Valid UsernameUpdateRequest usernameUpdateRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "user/update-username";
        }
        try {
            authenticationService.updateUsernameAndTokens(currentUser.getUsername(),
                    usernameUpdateRequest.getUsername(), response);
        }catch (UsernameTakenException ex){
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("usernameUpdateRequest", new UsernameUpdateRequest());
            return "user/update-username";
        }

        return "redirect:/api/user/me";
    }

    @GetMapping("/me/update-phone")
    public String showUpdatePhonePage(Model model){
        model.addAttribute("phoneUpdateRequest", new PhoneUpdateRequest());
        return "user/update-phone";
    }

    @PostMapping("/me/update-phone")
    public String updatePhone(
            @AuthenticationPrincipal UserDetails currentUser,
            @ModelAttribute("phoneUpdateRequest") @NotNull @Valid PhoneUpdateRequest phoneUpdateRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "user/update-phone";
        }

        userService.updateUser(currentUser.getUsername(), UserDTO.builder()
                .phoneNumber(phoneUpdateRequest.getPhoneNumber())
                .build());

        return "redirect:/api/user/me";
    }

    @GetMapping("/me/vouchers")
    public String showVouchersPage(@AuthenticationPrincipal UserDetails currentUser, Model model){
        UUID userId = userService.getUserByUsername(currentUser.getUsername()).getId();

        List<VoucherDTO> vouchers = voucherService.findAllByUserId(userId);

        model.addAttribute("vouchers", vouchers);

        return "user/my-vouchers";
    }

    @ModelAttribute("tourTypes")
    public TourType[] getTourTypes() {
        return TourType.values();
    }

    @ModelAttribute("transferTypes")
    public TransferType[] getTransferTypes() {
        return TransferType.values();
    }

    @ModelAttribute("hotelTypes")
    public HotelType[] getHotelTypes() {
        return HotelType.values();
    }

}
