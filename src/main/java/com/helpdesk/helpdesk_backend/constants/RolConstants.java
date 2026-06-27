package com.helpdesk.helpdesk_backend.constants;
import java.util.List;

public class RolConstants {
public static final String ADMIN_OWNER = "ADMIN_OWNER";

    public static final String ADMIN_EMPRESA = "ADMIN_EMPRESA";
    public static final String AGENTE = "AGENTE";
    public static final String CLIENTE = "CLIENTE";

    public static final List<String> ROLES_TENANT = List.of(
            ADMIN_EMPRESA,
            AGENTE,
            CLIENTE
    );

    /**
     * roles del sistema que NO pueden ser creados, renombrados, desactivados
     * ni modificados en sus permisos, son los anclajes del modelo de seguridad
     * (cambiarles el nombre o los permisos podria dejar al sistema sin admin
     * o permitir escalada de privilegios).
     */
    public static final List<String> ROLES_PROTEGIDOS = List.of(
            ADMIN_OWNER,
            ADMIN_EMPRESA
    );

    private RolConstants() {
    }
}
