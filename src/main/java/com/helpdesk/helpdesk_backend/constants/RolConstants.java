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

    private RolConstants() {
    }
}
