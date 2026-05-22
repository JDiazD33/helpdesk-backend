package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.service.impl.RolServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    void obtenerTodosLosRoles_DebeRetornarListaDeRoles(){
        Rol rol1 = Rol.builder().id(1L).nombre("ADMIN").build();
        Rol rol2 = Rol.builder().id(2L).nombre("USER").build();
        List<Rol> rolesMock = Arrays.asList(rol1,rol2);

        when(rolRepository.findAll()).thenReturn(rolesMock);

        List<Rol> resultado = rolService.listarTodos();
        
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("ADMIN");

        verify(rolRepository, times(1)).findAll();
    }
}
