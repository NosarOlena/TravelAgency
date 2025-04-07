package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherSearchCriteria;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final VoucherService voucherService;

    private final MessageSource messageSource;

    @GetMapping("/dashboard")
    public String showDashboardPage(){
        return "admin/admin-dashboard";
    }

    @GetMapping("/users/active")
    public String showActiveUsersPage(Model model) {
        List<UserDTO> activeUsers = userService.findAllActive();
        model.addAttribute("activeUsers", activeUsers);
        return "admin/active-users";
    }

    @PostMapping("/users/block/{userId}")
    public String blockUser(@PathVariable @NotNull UUID userId, RedirectAttributes redirectAttributes){
        UserDTO userDTO = userService.getUserById(userId);

        userDTO.setActive(false);
        userService.changeAccountStatus(userDTO);

        String message = messageSource.getMessage("user.status.blocked",
                new Object[]{userId}, LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/api/admin/users/active";
    }

    @GetMapping("/users/blocked")
    public String showBlockedUsersPage(Model model) {
        List<UserDTO> blockedUsers = userService.findAllBlocked();
        model.addAttribute("blockedUsers", blockedUsers);
        return "admin/blocked-users";
    }

    @PostMapping("/users/unblock/{userId}")
    public String unblockUser(@PathVariable @NotNull UUID userId, RedirectAttributes redirectAttributes){
        UserDTO userDTO = userService.getUserById(userId);

        userDTO.setActive(true);
        userService.changeAccountStatus(userDTO);

        String message = messageSource.getMessage("user.status.unblocked",
                new Object[]{userId}, LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/api/admin/users/blocked";
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

        return "admin/all-vouchers";
    }

    @PostMapping("/vouchers/delete/{voucherId}")
    public String deleteVoucher(@PathVariable @NotNull UUID voucherId, RedirectAttributes redirectAttributes){
        VoucherDTO voucher = voucherService.getVoucherById(voucherId);

        String status = voucher.getStatus();

        if (status.equals(VoucherStatus.PAID.name()) || status.equals(VoucherStatus.REGISTERED.name())) {
            String warningMessage = messageSource.getMessage(
                    "voucher.delete.warning",
                    new Object[]{voucher.getId(), VoucherStatus.valueOf(status)},
                    LocaleContextHolder.getLocale()
            );
            redirectAttributes.addFlashAttribute("warningMessage", warningMessage);
        }else{
            String successMessage = messageSource.getMessage(
                    "voucher.delete.success",
                    new Object[]{voucher.getId()},
                    LocaleContextHolder.getLocale()
            );
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        }

        voucherService.delete(voucherId);

        return "redirect:/api/admin/vouchers";
    }

    @GetMapping("/vouchers/update/{voucherId}")
    public String showUpdateVoucherPage(@PathVariable @NotNull UUID voucherId, Model model){
        VoucherDTO voucherDTO = voucherService.getVoucherById(voucherId);
        model.addAttribute("voucherDTO", voucherDTO);
        return "admin/update-voucher";
    }

    @PostMapping("/vouchers/update/{voucherId}")
    public String updateVoucher(@PathVariable @NotNull UUID voucherId,
                                @ModelAttribute("voucherDTO") @NotNull @Valid VoucherDTO updatedVoucher,
                                RedirectAttributes redirectAttributes) {

        voucherService.update(voucherId, updatedVoucher);

        String successMessage = messageSource.getMessage("voucher.update.success",
                new Object[]{voucherId}, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("successMessage", successMessage);

        return "redirect:/api/admin/vouchers";
    }

    @GetMapping("/vouchers/add")
    public String showCreateVoucherPage(Model model){
        model.addAttribute("voucherDTO", new VoucherDTO());
        return "admin/create-voucher";
    }

    @PostMapping("/vouchers/add")
    public String createVoucher(@ModelAttribute @NotNull @Valid VoucherDTO voucherDTO, RedirectAttributes redirectAttributes) {
        voucherService.create(voucherDTO);
        String message = messageSource.getMessage("voucher.create.success", null, LocaleContextHolder.getLocale());

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/api/admin/dashboard";
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
