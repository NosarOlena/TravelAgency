package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.validation.ValidEnum;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class UserDTO {

	private UUID id;

	@Size(min = 3, max = 50, message = "{user.username.size}")
	private String username;

	@Size(min = 8, message = "{user.password.size}")
	private String password;

	@ValidEnum(enumClass = Role.class, message = "{user.role.invalid}")
	private String role;

	private List<Voucher> vouchers;

	@Pattern(regexp = "^(\\+\\d{12}|\\d{10})$", message = "{user.phone-number.invalid}")
	private String phoneNumber;

	@PositiveOrZero(message = "{user.balance.negative}")
	private BigDecimal balance;

	private Boolean active;

}
