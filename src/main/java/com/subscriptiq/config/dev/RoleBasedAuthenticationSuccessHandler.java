package com.subscriptiq.config.dev;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String destination = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.equals("ROLE_ADMIN")
                        || authority.equals("ROLE_STAFF")
                        || authority.equals("ROLE_CUSTOMER"))
                .findFirst()
                .map(role -> switch (role) {
                    case "ROLE_CUSTOMER" -> "/portal";
                    case "ROLE_STAFF" -> "/staff/dashboard";
                    case "ROLE_ADMIN" -> "/admin/dashboard";
                    default -> throw new IllegalStateException("Unsupported authenticated role.");
                })
                .orElse(null);
        if (destination == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        response.sendRedirect(request.getContextPath() + destination);
    }
}
