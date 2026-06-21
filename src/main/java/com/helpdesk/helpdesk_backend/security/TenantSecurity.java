package com.helpdesk.helpdesk_backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Helper centralizado para la validación multi-tenant.
 *
 * - ADMIN_OWNER puede operar sobre cualquier empresa (vista global).
 * - Los demás roles (ADMIN_EMPRESA, AGENTE, CLIENTE) solo pueden
 *   operar sobre recursos de SU empresa (la del JWT).
 *
 * TODOS los controllers deben usar esta clase para obtener el empresaId
 * y validar el acceso, en lugar de leerlo de @RequestParam o @RequestHeader.
 */
@Component
public class TenantSecurity {

    /**
     * Devuelve el empresaId del usuario autenticado (del JWT).
     */
    public Long getEmpresaId() {
        return SecurityUtils.getCurrentUserEmpresaId();
    }

    /**
     * Devuelve el userId del usuario autenticado (del JWT).
     */
    public Long getUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * Devuelve el rol del usuario autenticado (del JWT).
     */
    public String getRol() {
        return SecurityUtils.getCurrentUserRol();
    }

    /**
     * ¿El usuario autenticado es ADMIN_OWNER?
     */
    public boolean isAdminOwner() {
        return SecurityUtils.isAdminOwner();
    }

    /**
     * Resuelve el empresaId que se debe usar para una operación:
     * - ADMIN_OWNER: usa el empresaId del parámetro (puede ver cualquier empresa).
     * - Otros roles: SIEMPRE usa el del JWT (ignora el parámetro).
     */
    public Long resolveEmpresaId(Long empresaIdFromParam) {
        if (isAdminOwner()) {
            return empresaIdFromParam;
        }
        return getEmpresaId();
    }

    /**
     * Valida que el usuario autenticado tiene acceso al recurso de la empresa indicada.
     * - ADMIN_OWNER: siempre pasa.
     * - Otros roles: lanza 403 si el empresaId del recurso no coincide con el del JWT.
     */
    public void checkTenantAccess(Long resourceEmpresaId) {
        if (isAdminOwner()) {
            return;
        }
        Long tokenEmpresaId = getEmpresaId();
        if (tokenEmpresaId == null || !tokenEmpresaId.equals(resourceEmpresaId)) {
            throw new AccessDeniedException(
                    "No tiene permiso para acceder a recursos de otra empresa");
        }
    }
}
