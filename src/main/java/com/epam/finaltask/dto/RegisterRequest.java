package com.epam.finaltask.dto;

import com.epam.finaltask.model.Role;
import com.epam.finaltask.validation.ValidEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotNull
    @Size(min = 3, max = 50, message = "{user.username.size}")
    private String username;

    @NotNull
    @Size(min = 8, message = "{user.password.size}")
    private String password;

    @Pattern(regexp = "^$|(\\+\\d{12}|\\d{10})$", message = "{user.phone-number.invalid}")
    private String phoneNumber;

    @ValidEnum(enumClass = Role.class, message = "{user.role.invalid}")
    @Builder.Default
    private String role = "USER";

}
