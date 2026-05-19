package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.ProblemaTicketMapper;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;
import com.helpdesk.helpdesk_backend.service.impl.ProblemaTicketServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemaTicketServiceImplTest {

    //Simula la base de datos
    @Mock
    private ProblemaTicketRepository problemaRepository;

    @Mock
    private CategoriaTicketRepository categoriaRepository;

    @Mock
    private ProblemaTicketMapper problemaMapper;

    //Inyecta el repositorio falso en el service real
    @InjectMocks
    private ProblemaTicketServiceImpl problemaService;

    private Empresa empresa;
    private CategoriaTicket categoria;
    private ProblemaTicket problema;
    private ProblemaRequestDTO requestDTO;
    private ProblemaResponseDTO responseDTO;

    private final Long EMPRESA_ID = 1L;
    private final Long CATEGORIA_ID = 10L;
    private final Long PROBLEMA_ID = 100L;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(EMPRESA_ID);

        categoria = new CategoriaTicket();
        categoria.setId(CATEGORIA_ID);
        categoria.setEmpresa(empresa);
        categoria.setActiva(true);

        problema = new ProblemaTicket();
        problema.setId(PROBLEMA_ID);
        problema.setNombre("Pantalla Rota");
        problema.setCategoria(categoria);
        problema.setActivo(true);

        requestDTO = new ProblemaRequestDTO();
        requestDTO.setNombre("Pantalla Rota");
        requestDTO.setCategoriaId(CATEGORIA_ID);

        responseDTO = new ProblemaResponseDTO();
        responseDTO.setId(PROBLEMA_ID);
        responseDTO.setNombre("Pantalla Rota");
        responseDTO.setCategoriaId(CATEGORIA_ID);
    }

    @Test
    void crearProblema_DebeGuardarEvitandoObjetoHuerfano() {
        // Arrange
        // Simulamos la búsqueda de la categoría para asegurar que pertenece a la empresa correcta
        when(categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(CATEGORIA_ID, EMPRESA_ID))
                .thenReturn(Optional.of(categoria));
        when(problemaRepository.existsByNombreAndCategoriaId(requestDTO.getNombre(), CATEGORIA_ID))
                .thenReturn(false);
        when(problemaMapper.toEntity(requestDTO)).thenReturn(problema);
        when(problemaRepository.save(any(ProblemaTicket.class))).thenReturn(problema);
        when(problemaMapper.toResponseDTO(problema)).thenReturn(responseDTO);

        // Act
        ProblemaResponseDTO resultado = problemaService.crearProblema(requestDTO, EMPRESA_ID);

        // Assert
        assertThat(resultado.getNombre()).isEqualTo("Pantalla Rota");
        verify(categoriaRepository).findByIdAndEmpresaIdAndActivaTrue(CATEGORIA_ID, EMPRESA_ID);
        verify(problemaRepository).save(problema);
    }

    @Test
    void crearProblema_DebeLanzarExcepcionSiCategoriaNoPerteneceAEmpresa() {
        // Arrange
        // Simulamos que la categoría NO existe para ese empresaId (Aislamiento)
        when(categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(CATEGORIA_ID, EMPRESA_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> problemaService.crearProblema(requestDTO, EMPRESA_ID));
        
        assertThat(exception.getMessage()).contains("Categoría no encontrada");
        verify(problemaRepository, never()).save(any());
    }

    @Test
    void eliminarProblema_DebeAplicarBorradoLogicoSiPasaValidacionTransitiva() {
        // Arrange
        when(problemaRepository.findById(PROBLEMA_ID)).thenReturn(Optional.of(problema));

        // Act
        problemaService.eliminarProblema(PROBLEMA_ID, EMPRESA_ID);

        // Assert
        assertThat(problema.isActivo()).isFalse(); // Verificamos el borrado lógico
        verify(problemaRepository).save(problema);
    }

    @Test
    void eliminarProblema_DebeRechazarSiIntentaEliminarProblemaDeOtraEmpresa() {
        // Arrange
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        categoria.setEmpresa(otraEmpresa); // El problema pertenece a otra empresa indirectamente
        
        when(problemaRepository.findById(PROBLEMA_ID)).thenReturn(Optional.of(problema));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> problemaService.eliminarProblema(PROBLEMA_ID, EMPRESA_ID));
        
        assertThat(exception.getMessage()).contains("No tiene permisos");
        verify(problemaRepository, never()).save(any());
    }
}