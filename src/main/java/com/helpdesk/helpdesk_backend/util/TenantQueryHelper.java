package com.helpdesk.helpdesk_backend.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.helpdesk.helpdesk_backend.constants.RolConstants;

/**
 * Helper centralizado para aplicar el filtro multi-tenant que excluye al
 * ADMIN_OWNER de las consultas de un tenant.
 *
 * El owner del SaaS está artificialmente asociado a una empresa por la
 * restricción NOT NULL de la columna empresa_id, pero NO pertenece
 * lógicamente a ningún tenant. Nunca debe contar en las métricas,
 * rankings, listados ni conteos de un tenant.
 */
@Component
public class TenantQueryHelper {

    /**
     * Devuelve el nombre exacto del rol del owner del SaaS.
     * Único punto de lectura del valor para excluirlo de las queries de tenant.
     */
    public String getRolOwner() {
        return RolConstants.ADMIN_OWNER;
    }

    /**
     * Verifica si un nombre de rol corresponde al owner del SaaS.
     */
    public boolean esOwnerSaaS(String rol) {
        return RolConstants.ADMIN_OWNER.equals(rol);
    }

    /**
     * Verifica si un rol pertenece a un tenant (no es el owner del SaaS).
     */
    public boolean esRolTenant(String rol) {
        return rol != null && RolConstants.ROLES_TENANT.contains(rol);
    }

    /**
     * Filtra en memoria una lista ya cargada, removiendo al ADMIN_OWNER.
     * Útil cuando una API externa no soporta el filtro en query
     * o cuando se trabaja con DTOs que ya tienen el nombre del rol.
     */
    public <T> List<T> excluirOwner(List<T> entidades, Function<T, String> getRol) {
        if (entidades == null) {
            return List.of();
        }
        return entidades.stream()
                .filter(e -> !esOwnerSaaS(getRol.apply(e)))
                .collect(Collectors.toList());
    }
}
