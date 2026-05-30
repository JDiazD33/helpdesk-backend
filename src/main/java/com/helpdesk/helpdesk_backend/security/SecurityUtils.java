package com.helpdesk.helpdesk_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UsuarioPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof UsuarioPrincipal)) {
            return null;
        }
        return (UsuarioPrincipal) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        UsuarioPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static Long getCurrentUserEmpresaId() {
        UsuarioPrincipal user = getCurrentUser();
        return user != null ? user.getEmpresaId() : null;
    }

    public static String getCurrentUserRol() {
        UsuarioPrincipal user = getCurrentUser();
        return user != null ? user.getRol() : null;
    }

    public static boolean isAdminOwner() {
        return "ADMIN_OWNER".equals(getCurrentUserRol());
    }

    public static boolean isAdminEmpresa() {
        return "ADMIN_EMPRESA".equals(getCurrentUserRol());
    }
}
