package com.helpdesk.helpdesk_backend.config;

import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.model.Rol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederConfigTest {

    @Mock
    private RolRepository rolRepository;

    @Test
    void initRoles_creaRoles_cuandoNoExisten() throws Exception {
        when(rolRepository.count()).thenReturn(0L);

        DataSeederConfig config = new DataSeederConfig();
        CommandLineRunner runner = config.initRoles(rolRepository);
        runner.run();

        ArgumentCaptor<List<Rol>> captor = ArgumentCaptor.forClass(List.class);
        verify(rolRepository).saveAll(captor.capture());

        List<Rol> rolesGuardados = captor.getValue();
        assertEquals(3, rolesGuardados.size());
        assertTrue(rolesGuardados.stream().anyMatch(r -> r.getNombre().equals("ADMIN_EMPRESA")));
        assertTrue(rolesGuardados.stream().anyMatch(r -> r.getNombre().equals("AGENTE")));
        assertTrue(rolesGuardados.stream().anyMatch(r -> r.getNombre().equals("CLIENTE")));
    }

    @Test
    void initRoles_noCreaRoles_cuandoYaExisten() throws Exception {
        when(rolRepository.count()).thenReturn(3L);

        DataSeederConfig config = new DataSeederConfig();
        CommandLineRunner runner = config.initRoles(rolRepository);
        runner.run();

        verify(rolRepository, never()).saveAll(anyList());
    }
}