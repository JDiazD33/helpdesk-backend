package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.CategoriaTicketMapper;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.impl.CategoriaTicketServiceImpl;

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
class CategoriaTicketServiceImplTest {

    @Mock
    private CategoriaTicketRepository categoriaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private CategoriaTicketMapper categoriaMapper;

    @InjectMocks
    private CategoriaTicketServiceImpl categoriaService;

    private Empresa empresa;
    private CategoriaTicket categoria;
    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;

    private final Long EMPRESA_ID = 1L;
    private final Long CATEGORIA_ID = 10L;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(EMPRESA_ID);

        categoria = new CategoriaTicket();
        categoria.setId(CATEGORIA_ID);
        categoria.setNombre("Hardware");
        categoria.setEmpresa(empresa);
        categoria.setActiva(true);

        requestDTO = new CategoriaRequestDTO();
        requestDTO.setNombre("Hardware");

        responseDTO = new CategoriaResponseDTO();
        responseDTO.setId(CATEGORIA_ID);
        responseDTO.setNombre("Hardware");
    }

    @Test
    void crearCategoria_DebeGuardarExitosamente() {
        // Arrange
        when(categoriaRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), EMPRESA_ID)).thenReturn(false);
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
        when(categoriaMapper.toEntity(requestDTO)).thenReturn(categoria);
        when(categoriaRepository.save(any(CategoriaTicket.class))).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        // Act
        CategoriaResponseDTO resultado = categoriaService.crearCategoria(requestDTO, EMPRESA_ID);

        // Assert
        assertThat(resultado.getNombre()).isEqualTo("Hardware");
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void crearCategoria_DebeLanzarExcepcionSiNombreDuplicado() {
        // Arrange
        when(categoriaRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), EMPRESA_ID)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> categoriaService.crearCategoria(requestDTO, EMPRESA_ID));
        
        assertThat(exception.getMessage()).contains("Ya existe una categoría con este nombre");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void eliminarCategoria_DebeAplicarBorradoLogico() {
        // Arrange
        when(categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(CATEGORIA_ID, EMPRESA_ID))
                .thenReturn(Optional.of(categoria));

        // Act
        categoriaService.eliminarCategoria(CATEGORIA_ID, EMPRESA_ID);

        // Assert
        assertThat(categoria.isActiva()).isFalse(); // Verificamos el borrado lógico
        verify(categoriaRepository).save(categoria);
    }
}