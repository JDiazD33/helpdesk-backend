package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.RolMapper;
import com.helpdesk.helpdesk_backend.model.Rol;
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

    // Mock del repositorio
    @Mock
    private RolRepository rolRepository;

    @Mock
    private RolMapper rolMapper;

    // Inyección del mock en el servicio
    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    void obtenerTodosLosRoles_DebeRetornarListaDeRolResponseDTO(){
        // Arrage
        Rol rol1 = Rol.builder().id(1L).nombre("ADMIN").build();
        Rol rol2 = Rol.builder().id(2L).nombre("USER").build();
        List<Rol> rolesMock = Arrays.asList(rol1,rol2);

        RolResponseDTO dto1 = RolResponseDTO.builder().id(1L).nombre("ADMIN").build();
        RolResponseDTO dto2 = RolResponseDTO.builder().id(2L).nombre("USER").build();

        // Simulamos el comportamiento del repositorio y del mapper
        when(rolRepository.findAll()).thenReturn(rolesMock);
        when(rolMapper.toResponseDTO(rol1)).thenReturn(dto1);
        when(rolMapper.toResponseDTO(rol2)).thenReturn(dto2);

        // Act
        List<RolResponseDTO> resultado = rolService.obtenerTodosLosRoles();
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("ADMIN");

        // Verificamos que los métodos simulados hayan sido llamados
        verify(rolRepository, times(1)).findAll();
        verify(rolMapper, times(1)).toResponseDTO(rol1);
        verify(rolMapper, times(1)).toResponseDTO(rol2);
        
    }
    
}
