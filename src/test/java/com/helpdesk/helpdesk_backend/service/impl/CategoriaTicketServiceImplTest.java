package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaTicketServiceImplTest {

    @Mock
    private CategoriaTicketRepository categoriaTicketRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private CategoriaTicketServiceImpl categoriaTicketService;

    private Empresa empresa;
    private CategoriaTicket categoria;
    private CategoriaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombre("Tech Solutions")
                .build();

        categoria = CategoriaTicket.builder()
                .id(1L)
                .nombre("Soporte Técnico")
                .descripcion("Problemas técnicos")
                .activa(true)
                .empresa(empresa)
                .build();

        requestDTO = new CategoriaRequestDTO();
        requestDTO.setNombre("Soporte Técnico");
        requestDTO.setDescripcion("Problemas técnicos");
    }

    @Test
    void listarCategoriasActivas_debeRetornarLista() {
        when(categoriaTicketRepository.findActivasPorEmpresaOrdenadas(1L))
                .thenReturn(List.of(categoria));

        List<CategoriaResponseDTO> resultado = categoriaTicketService.listarCategoriasActivas(1L);

        assertEquals(1, resultado.size());
        assertEquals("Soporte Técnico", resultado.get(0).getNombre());
    }

    @Test
    void listarTodas_debeRetornarLista() {
        when(categoriaTicketRepository.findByEmpresaId(1L))
                .thenReturn(List.of(categoria));

        List<CategoriaResponseDTO> resultado = categoriaTicketService.listarTodas(1L);

        assertEquals(1, resultado.size());
        assertEquals("Soporte Técnico", resultado.get(0).getNombre());
    }

    @Test
    void obtenerPorId_debeRetornarCategoria() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaResponseDTO resultado = categoriaTicketService.obtenerPorId(1L, 1L);

        assertEquals("Soporte Técnico", resultado.getNombre());
        assertTrue(resultado.isActiva());
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaTicketService.obtenerPorId(99L, 1L));
    }

    @Test
    void obtenerPorId_empresaNoCoincide_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ResourceNotFoundException.class, () -> categoriaTicketService.obtenerPorId(1L, 99L));
    }

    @Test
    void actualizarCategoria_mismoNombre_noVerificaDuplicado() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaTicketRepository.save(any(CategoriaTicket.class))).thenReturn(categoria);

        // mismo nombre que ya tiene la categoría → no entra al &&
        CategoriaRequestDTO mismoNombre = new CategoriaRequestDTO();
        mismoNombre.setNombre("Soporte Técnico"); // igual al existente
        mismoNombre.setDescripcion("Nueva descripción");

        CategoriaResponseDTO resultado = categoriaTicketService.actualizarCategoria(1L, mismoNombre, 1L);

        assertNotNull(resultado);
        verify(categoriaTicketRepository, never()).existsByNombreAndEmpresaId(any(), any());
        verify(categoriaTicketRepository).save(any(CategoriaTicket.class));
    }

    @Test
    void actualizarCategoria_nombreDiferenteYNoDuplicado_debeActualizar() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaTicketRepository.existsByNombreAndEmpresaId("Nombre Nuevo", 1L))
                .thenReturn(false); // nombre diferente pero no duplicado
        when(categoriaTicketRepository.save(any(CategoriaTicket.class))).thenReturn(categoria);

        CategoriaRequestDTO nuevoRequest = new CategoriaRequestDTO();
        nuevoRequest.setNombre("Nombre Nuevo"); // diferente al existente "Soporte Técnico"
        nuevoRequest.setDescripcion("Nueva descripción");

        CategoriaResponseDTO resultado = categoriaTicketService.actualizarCategoria(1L, nuevoRequest, 1L);

        assertNotNull(resultado);
        verify(categoriaTicketRepository).existsByNombreAndEmpresaId("Nombre Nuevo", 1L);
        verify(categoriaTicketRepository).save(any(CategoriaTicket.class));
    }

    @Test
    void crearCategoria_debeCrearYRetornarDTO() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(categoriaTicketRepository.existsByNombreAndEmpresaId("Soporte Técnico", 1L))
                .thenReturn(false);
        when(categoriaTicketRepository.save(any(CategoriaTicket.class))).thenReturn(categoria);

        CategoriaResponseDTO resultado = categoriaTicketService.crearCategoria(requestDTO, 1L);

        assertEquals("Soporte Técnico", resultado.getNombre());
        verify(categoriaTicketRepository).save(any(CategoriaTicket.class));
    }

    @Test
    void crearCategoria_empresaNoExiste_lanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaTicketService.crearCategoria(requestDTO, 99L));
    }

    @Test
    void crearCategoria_nombreDuplicado_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(categoriaTicketRepository.existsByNombreAndEmpresaId("Soporte Técnico", 1L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoriaTicketService.crearCategoria(requestDTO, 1L));
    }

    @Test
    void actualizarCategoria_debeActualizarYRetornarDTO() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaTicketRepository.save(any(CategoriaTicket.class))).thenReturn(categoria);

        CategoriaRequestDTO nuevoRequest = new CategoriaRequestDTO();
        nuevoRequest.setNombre("Soporte Técnico");
        nuevoRequest.setDescripcion("Nueva descripción");

        CategoriaResponseDTO resultado = categoriaTicketService.actualizarCategoria(1L, nuevoRequest, 1L);

        assertNotNull(resultado);
        verify(categoriaTicketRepository).save(any(CategoriaTicket.class));
    }

    @Test
    void actualizarCategoria_noExiste_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoriaTicketService.actualizarCategoria(99L, requestDTO, 1L));
    }

    @Test
    void actualizarCategoria_empresaNoCoincide_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ResourceNotFoundException.class,
                () -> categoriaTicketService.actualizarCategoria(1L, requestDTO, 99L));
    }

    @Test
    void actualizarCategoria_nombreDuplicado_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaTicketRepository.existsByNombreAndEmpresaId("Nuevo Nombre", 1L))
                .thenReturn(true);

        CategoriaRequestDTO nuevoRequest = new CategoriaRequestDTO();
        nuevoRequest.setNombre("Nuevo Nombre");

        assertThrows(DuplicateResourceException.class,
                () -> categoriaTicketService.actualizarCategoria(1L, nuevoRequest, 1L));
    }

    @Test
    void eliminarCategoria_debeDesactivarCategoria() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        categoriaTicketService.eliminarCategoria(1L, 1L);

        assertFalse(categoria.isActiva());
        verify(categoriaTicketRepository).save(categoria);
    }

    @Test
    void eliminarCategoria_noExiste_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaTicketService.eliminarCategoria(99L, 1L));
    }

    @Test
    void eliminarCategoria_empresaNoCoincide_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ResourceNotFoundException.class, () -> categoriaTicketService.eliminarCategoria(1L, 99L));
    }
}