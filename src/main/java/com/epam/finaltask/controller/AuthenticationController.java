package com.epam.finaltask.controller;

import com.epam.finaltask.dto.LoginRequest;
import com.epam.finaltask.dto.RegisterRequest;
import com.epam.finaltask.exception.AuthenticationFailedException;
import com.epam.finaltask.exception.UsernameTakenException;
import com.epam.finaltask.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final MessageSource messageSource;

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/sign-up")
    public String signUp(
            @ModelAttribute("registerRequest") @Valid RegisterRequest registerRequest,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }
        try {
            authenticationService.register(registerRequest, response);
        }catch (UsernameTakenException ex){
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "auth/sign-up";
        }

        return "redirect:/api/auth/redirect-by-role";
    }

    @GetMapping("/sign-up")
    public String showSignUpPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/sign-up";
    }

    @PostMapping("/sign-in")
    public String signIn(@ModelAttribute("loginRequest") LoginRequest loginRequest, HttpServletResponse response) {
        try{
            authenticationService.login(loginRequest, response);
        }catch (InternalAuthenticationServiceException e){
            throw new AuthenticationFailedException(
                    messageSource.getMessage("auth.account.credentials", null, LocaleContextHolder.getLocale()),
                    e
            );
        }
        return "redirect:/api/auth/redirect-by-role";
    }

    @GetMapping("/sign-in")
    public String showSignInPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/sign-in";
    }

    @GetMapping("/redirect-by-role")
    public String redirectByRole(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isAdmin) {
            return "redirect:/api/admin/dashboard";
        } else if (isManager) {
            return "redirect:/api/manager/dashboard";
        }else if (isUser) {
            return "redirect:/api/user/dashboard";
        } else {
            return "redirect:/";
        }
    }

}
