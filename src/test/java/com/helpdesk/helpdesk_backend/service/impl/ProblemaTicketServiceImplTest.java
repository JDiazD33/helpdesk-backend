package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;

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
class ProblemaTicketServiceImplTest {

    @Mock
    private ProblemaTicketRepository problemaTicketRepository;

    @Mock
    private CategoriaTicketRepository categoriaTicketRepository;

    @InjectMocks
    private ProblemaTicketServiceImpl problemaTicketService;

    private Empresa empresa;
    private CategoriaTicket categoria;
    private ProblemaTicket problema;
    private ProblemaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombre("Tech Solutions")
                .build();

        categoria = CategoriaTicket.builder()
                .id(1L)
                .nombre("Soporte Técnico")
                .empresa(empresa)
                .build();

        problema = ProblemaTicket.builder()
                .id(1L)
                .nombre("Falla de red")
                .descripcion("Sin conexión")
                .activo(true)
                .categoria(categoria)
                .build();

        requestDTO = new ProblemaRequestDTO();
        requestDTO.setNombre("Falla de red");
        requestDTO.setDescripcion("Sin conexión");
        requestDTO.setCategoriaId(1L);
    }

    @Test
    void listarActivosPorEmpresa_debeRetornarLista() {
        when(problemaTicketRepository.findActivosPorEmpresa(1L))
                .thenReturn(List.of(problema));

        List<ProblemaResponseDTO> resultado = problemaTicketService.listarActivosPorEmpresa(1L);

        assertEquals(1, resultado.size());
        assertEquals("Falla de red", resultado.get(0).getNombre());
    }

    @Test
    void obtenerPorId_debeRetornarProblema() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));

        ProblemaResponseDTO resultado = problemaTicketService.obtenerPorId(1L, 1L);

        assertEquals("Falla de red", resultado.getNombre());
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(problemaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.obtenerPorId(99L, 1L));
    }

    @Test
    void obtenerPorId_empresaNoCoincide_lanzaExcepcion() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.obtenerPorId(1L, 99L));
    }

    @Test
    void listarProblemasPorCategoria_debeRetornarLista() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(problemaTicketRepository.findAllByCategoriaIdAndActivoTrue(1L))
                .thenReturn(List.of(problema));

        List<ProblemaResponseDTO> resultado = problemaTicketService.listarProblemasPorCategoria(1L, 1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void listarProblemasPorCategoria_categoriaNoExiste_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.listarProblemasPorCategoria(99L, 1L));
    }

    @Test
    void listarProblemasPorCategoria_empresaNoCoincide_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.listarProblemasPorCategoria(1L, 99L));
    }

    @Test
    void crearProblema_debeCrearYRetornarDTO() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(problemaTicketRepository.existsByNombreAndCategoriaId("Falla de red", 1L))
                .thenReturn(false);
        when(problemaTicketRepository.save(any(ProblemaTicket.class))).thenReturn(problema);

        ProblemaResponseDTO resultado = problemaTicketService.crearProblema(requestDTO, 1L);

        assertEquals("Falla de red", resultado.getNombre());
        verify(problemaTicketRepository).save(any(ProblemaTicket.class));
    }

    @Test
    void crearProblema_categoriaNoExiste_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.crearProblema(requestDTO, 1L));
    }

    @Test
    void crearProblema_empresaNoCoincide_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.crearProblema(requestDTO, 99L));
    }

    @Test
    void crearProblema_nombreDuplicado_lanzaExcepcion() {
        when(categoriaTicketRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(problemaTicketRepository.existsByNombreAndCategoriaId("Falla de red", 1L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                problemaTicketService.crearProblema(requestDTO, 1L));
    }

    @Test
    void actualizarProblema_debeActualizarYRetornarDTO() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(problemaTicketRepository.save(any(ProblemaTicket.class))).thenReturn(problema);

        ProblemaResponseDTO resultado = problemaTicketService.actualizarProblema(1L, requestDTO, 1L);

        assertNotNull(resultado);
        verify(problemaTicketRepository).save(any(ProblemaTicket.class));
    }

    @Test
    void actualizarProblema_noExiste_lanzaExcepcion() {
        when(problemaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.actualizarProblema(99L, requestDTO, 1L));
    }

    @Test
    void actualizarProblema_empresaNoCoincide_lanzaExcepcion() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.actualizarProblema(1L, requestDTO, 99L));
    }

    @Test
    void actualizarProblema_nombreDuplicado_lanzaExcepcion() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(problemaTicketRepository.existsByNombreAndCategoriaId("Nuevo Nombre", 1L))
                .thenReturn(true);

        ProblemaRequestDTO nuevoRequest = new ProblemaRequestDTO();
        nuevoRequest.setNombre("Nuevo Nombre");
        nuevoRequest.setCategoriaId(1L);

        assertThrows(DuplicateResourceException.class, () ->
                problemaTicketService.actualizarProblema(1L, nuevoRequest, 1L));
    }

    @Test
    void actualizarProblema_nombreDiferenteNoDuplicado_debeActualizar() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(problemaTicketRepository.existsByNombreAndCategoriaId("Nuevo Nombre", 1L))
                .thenReturn(false);
        when(problemaTicketRepository.save(any(ProblemaTicket.class))).thenReturn(problema);

        ProblemaRequestDTO nuevoRequest = new ProblemaRequestDTO();
        nuevoRequest.setNombre("Nuevo Nombre");
        nuevoRequest.setCategoriaId(1L);

        ProblemaResponseDTO resultado = problemaTicketService.actualizarProblema(1L, nuevoRequest, 1L);

        assertNotNull(resultado);
        verify(problemaTicketRepository).existsByNombreAndCategoriaId("Nuevo Nombre", 1L);
    }

    @Test
    void actualizarProblema_cambiandoCategoria_debeActualizar() {
        CategoriaTicket nuevaCategoria = CategoriaTicket.builder()
                .id(2L)
                .nombre("Nueva Categoría")
                .empresa(empresa)
                .build();

        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(categoriaTicketRepository.findById(2L)).thenReturn(Optional.of(nuevaCategoria));
        when(problemaTicketRepository.save(any(ProblemaTicket.class))).thenReturn(problema);

        ProblemaRequestDTO nuevoRequest = new ProblemaRequestDTO();
        nuevoRequest.setNombre("Falla de red");
        nuevoRequest.setCategoriaId(2L); // categoría diferente

        ProblemaResponseDTO resultado = problemaTicketService.actualizarProblema(1L, nuevoRequest, 1L);

        assertNotNull(resultado);
        verify(categoriaTicketRepository).findById(2L);
    }

    @Test
    void actualizarProblema_nuevaCategoriaNoExiste_lanzaExcepcion() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(categoriaTicketRepository.findById(2L)).thenReturn(Optional.empty());

        ProblemaRequestDTO nuevoRequest = new ProblemaRequestDTO();
        nuevoRequest.setNombre("Falla de red");
        nuevoRequest.setCategoriaId(2L);

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.actualizarProblema(1L, nuevoRequest, 1L));
    }

    @Test
    void actualizarProblema_nuevaCategoriaEmpresaNoCoincide_lanzaExcepcion() {
        Empresa otraEmpresa = Empresa.builder().id(99L).nombre("Otra").build();
        CategoriaTicket categoriaOtraEmpresa = CategoriaTicket.builder()
                .id(2L)
                .nombre("Otra Categoría")
                .empresa(otraEmpresa)
                .build();

        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));
        when(categoriaTicketRepository.findById(2L)).thenReturn(Optional.of(categoriaOtraEmpresa));

        ProblemaRequestDTO nuevoRequest = new ProblemaRequestDTO();
        nuevoRequest.setNombre("Falla de red");
        nuevoRequest.setCategoriaId(2L);

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.actualizarProblema(1L, nuevoRequest, 1L));
    }

    @Test
    void eliminarProblema_debeDesactivar() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));

        problemaTicketService.eliminarProblema(1L, 1L);

        assertFalse(problema.isActivo());
        verify(problemaTicketRepository).save(problema);
    }

    @Test
    void eliminarProblema_noExiste_lanzaExcepcion() {
        when(problemaTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.eliminarProblema(99L, 1L));
    }

    @Test
    void eliminarProblema_empresaNoCoincide_lanzaExcepcion() {
        when(problemaTicketRepository.findById(1L)).thenReturn(Optional.of(problema));

        assertThrows(ResourceNotFoundException.class, () ->
                problemaTicketService.eliminarProblema(1L, 99L));
    }
}