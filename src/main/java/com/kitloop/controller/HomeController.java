package com.kitloop.controller;

import com.kitloop.dto.RegistrationRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String authenticationPage(
            Authentication authentication,
            @RequestParam(
                    name = "error",
                    required = false
            )
            String error,
            @RequestParam(
                    name = "logout",
                    required = false
            )
            String logout,
            @RequestParam(
                    name = "registered",
                    required = false
            )
            String registered,
            @RequestParam(
                    name = "registrationExpired",
                    required = false
            )
            String registrationExpired,
            Model model
    ) {

        if (hasAuthority(
                authentication,
                "ROLE_ADMIN"
        )) {
            return "redirect:/admin/dashboard";
        }

        if (hasAuthority(
                authentication,
                "ROLE_USER"
        )) {
            return "redirect:/user/dashboard";
        }

        model.addAttribute(
                "registrationRequest",
                new RegistrationRequest()
        );

        model.addAttribute(
                "activeTab",
                "signin"
        );

        model.addAttribute(
                "loginError",
                error != null
        );

        model.addAttribute(
                "logoutSuccess",
                logout != null
        );

        model.addAttribute(
                "registrationSuccess",
                registered != null
        );

        model.addAttribute(
                "registrationExpired",
                registrationExpired != null
        );

        return "index";
    }

    @GetMapping("/post-login")
    public String postLogin(
            Authentication authentication
    ) {

        if (hasAuthority(
                authentication,
                "ROLE_ADMIN"
        )) {
            return "redirect:/admin/dashboard";
        }

        if (hasAuthority(
                authentication,
                "ROLE_USER"
        )) {
            return "redirect:/user/dashboard";
        }

        return "redirect:/?error";
    }

    @GetMapping("/access-denied")
    public String accessDenied(
            Authentication authentication,
            Model model
    ) {

        if (authentication != null) {
            model.addAttribute(
                    "email",
                    authentication.getName()
            );
        }

        return "errors/403";
    }

    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        authority.equals(
                                grantedAuthority
                                        .getAuthority()
                        )
                );
    }
}