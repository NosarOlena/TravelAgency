package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherSearchCriteria;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/api/manager")
public class ManagerController {

    private final VoucherService voucherService;
    private final MessageSource messageSource;

    @GetMapping("/dashboard")
    public String showDashboardPage(@AuthenticationPrincipal UserDetails currentUser, Model model){
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("isAdmin", isAdmin);
        return "manager/manager-dashboard";
    }

    @GetMapping("/vouchers")
    public String showAllVouchersPage(@RequestParam(required = false) String title,
                                      @RequestParam(required = false) String description,
                                      @RequestParam(required = false) String tourType,
                                      @RequestParam(required = false) String transferType,
                                      @RequestParam(required = false) String hotelType,
                                      @RequestParam(required = false) LocalDate arrivalFrom,
                                      @RequestParam(required = false) LocalDate evictionTo,
                                      @RequestParam(required = false) BigDecimal minPrice,
                                      @RequestParam(required = false) BigDecimal maxPrice,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) Boolean hotStatus,
                                      Model model) {

        TourType tourTypeEnum = (tourType != null && !tourType.isBlank()) ? TourType.valueOf(tourType) : null;
        TransferType transferTypeEnum = (transferType != null && !transferType.isBlank()) ? TransferType.valueOf(transferType) : null;
        HotelType hotelTypeEnum = (hotelType != null && !hotelType.isBlank()) ? HotelType.valueOf(hotelType) : null;
        VoucherStatus voucherStatusEnum = (status != null && !status.isBlank()) ? VoucherStatus.valueOf(status) : null;

        VoucherSearchCriteria criteria = VoucherSearchCriteria.builder()
                .title(title)
                .description(description)
                .tourType(tourTypeEnum)
                .transferType(transferTypeEnum)
                .hotelType(hotelTypeEnum)
                .status(voucherStatusEnum)
                .arrivalFrom(arrivalFrom)
                .evictionTo(evictionTo)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .hotStatus(hotStatus)
                .build();

        List<VoucherDTO> vouchers = voucherService.filterVouchers(criteria, false);

        model.addAttribute("vouchers", vouchers);

        return "manager/all-vouchers";
    }

    @PostMapping("/vouchers/change-hot-status/{voucherId}")
    public String changeVoucherHotStatus(@PathVariable @NotNull UUID voucherId, RedirectAttributes redirectAttributes) {
        voucherService.changeHotStatus(voucherId);

        String message = messageSource.getMessage("voucher.change-hot-status.success",
                new Object[]{voucherId}, LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/api/manager/vouchers";
    }

    @PostMapping("/vouchers/change-status/{voucherId}")
    public String changeVoucherStatus(
            @PathVariable @NotNull UUID voucherId,
            @RequestParam @NotNull String newStatus,
            RedirectAttributes redirectAttributes) {
        voucherService.changeStatus(voucherId, VoucherDTO.builder().status(newStatus).build());

        String message = messageSource.getMessage("voucher.change-status.success",
                new Object[]{voucherId, newStatus}, LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/api/manager/vouchers";
    }

    @ModelAttribute("availableStatuses")
    public VoucherStatus[] getAvailableStatuses(){
        return new VoucherStatus[]{VoucherStatus.PAID, VoucherStatus.CANCELLED};
    }

    @ModelAttribute("statusForChange")
    public VoucherStatus getStatusForChange(){
        return VoucherStatus.REGISTERED;
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

    @ModelAttribute("voucherStatuses")
    public VoucherStatus[] getVoucherStatuses() {
        return VoucherStatus.values();
    }

}
