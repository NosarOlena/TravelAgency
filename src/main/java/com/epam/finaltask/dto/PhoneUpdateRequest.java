package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhoneUpdateRequest {

    @NotNull
    @Pattern(regexp = "^(\\+\\d{12}|\\d{10})$", message = "{user.phone-number.invalid}")
    private String phoneNumber;
}
