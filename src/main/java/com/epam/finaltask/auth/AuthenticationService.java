package com.epam.finaltask.auth;

import com.epam.finaltask.dto.LoginRequest;
import com.epam.finaltask.dto.RegisterRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AuthenticationFailedException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final CookieService cookieService;

    public void register(RegisterRequest request, HttpServletResponse response) {
        String phoneNumber = request.getPhoneNumber();

        if(phoneNumber != null && phoneNumber.isBlank()){
            phoneNumber = null;
        }

        UserDTO userDto = UserDTO.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .phoneNumber(phoneNumber)
                .role(request.getRole())
                .balance("USER".equals(request.getRole()) ? BigDecimal.ZERO : null)
                .active(true)
                .build();

        UserDTO registeredUserDto = userService.register(userDto);
        User user = userMapper.toUser(registeredUserDto);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        cookieService.setAccessTokenCookie(response, accessToken);
        cookieService.setRefreshTokenCookie(response, refreshToken);
    }

    public void login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (DisabledException e) {
            throw new AuthenticationFailedException(
                    messageSource.getMessage("auth.account.disabled", null, LocaleContextHolder.getLocale()),
                    e
            );
        } catch (LockedException e) {
            throw new AuthenticationFailedException(
                    messageSource.getMessage("auth.account.locked", null, LocaleContextHolder.getLocale()),
                    e
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException(
                    messageSource.getMessage("auth.account.credentials", null, LocaleContextHolder.getLocale()),
                    e
            );
        }

        UserDTO userDto = userService.getUserByUsername(request.getUsername());
        User user = userMapper.toUser(userDto);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        cookieService.setAccessTokenCookie(response, accessToken);
        cookieService.setRefreshTokenCookie(response, refreshToken);

    }

    public void updateUsernameAndTokens(String currentUsername, String newUsername, HttpServletResponse response) {
        UserDTO updatedUserDto = userService.updateUser(currentUsername, UserDTO.builder()
                .username(newUsername)
                .build());

        User updatedUser = userMapper.toUser(updatedUserDto);

        String newAccessToken = jwtService.generateToken(updatedUser);
        String newRefreshToken = jwtService.generateRefreshToken(updatedUser);

        cookieService.setAccessTokenCookie(response, newAccessToken);
        cookieService.setRefreshTokenCookie(response, newRefreshToken);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return;
        }

        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
            cookieService.clearRefreshTokenCookie(response);
            return;
        }

        UserDTO userDto = userService.getUserByUsername(username);
        if (userDto == null) {
            cookieService.clearRefreshTokenCookie(response);
            return;
        }

        User user = userMapper.toUser(userDto);

        if (!jwtService.isTokenValid(refreshToken, user)) {
            cookieService.clearRefreshTokenCookie(response);
            return;
        }

        String newAccessToken = jwtService.generateToken(user);
        cookieService.setAccessTokenCookie(response, newAccessToken);
    }

}
