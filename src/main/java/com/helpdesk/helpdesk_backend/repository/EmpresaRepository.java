package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.dto.EmpresaTicketsDTO;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

        // Método para buscar una empresa por su RUC y que esté activa
        Optional<Empresa> findByRucAndActivoTrue(String ruc);

        // Método para buscar una empresa por su correo de contacto y que esté activa
        Optional<Empresa> findByCorreoContactoAndActivoTrue(String correoContacto);

        // Método para buscar una empresa por su ID y que esté activa
        Optional<Empresa> findByIdAndActivoTrue(Long id);

        // Método para buscar una empresa por su RUC sin importar si está activa o no
        Optional<Empresa> findByRuc(String ruc);

        boolean existsByRuc(String ruc);

        boolean existsByCorreoContacto(String correoContacto);

        // Buscar empresa por nombre
        @Query("""
                        SELECT e FROM Empresa e
                        WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                        """)
        List<Empresa> buscarPorNombre(@Param("texto") String texto);

        // Listar empresas activas ordenadas alfabéticamente
        @Query("""
                        SELECT e FROM Empresa e
                        WHERE e.activo = true
                        ORDER BY e.nombre ASC
                        """)
        List<Empresa> listarEmpresasActivas();

        // Empresas registradas recientemente
        @Query("""
                        SELECT e FROM Empresa e
                        WHERE e.fechaCreacion >= :fecha
                        ORDER BY e.fechaCreacion DESC
                        """)
        List<Empresa> empresasRecientes(@Param("fecha") LocalDateTime fecha);

        // Ranking de empresas con mayor cantidad de tickets
        @Query("""
                        SELECT new com.helpdesk.helpdesk_backend.dto.EmpresaTicketsDTO(
                            e.nombre,
                            COUNT(t)
                        )
                        FROM Empresa e
                        JOIN Ticket t ON t.empresa.id = e.id
                        GROUP BY e.nombre
                        ORDER BY COUNT(t) DESC
                        """)
        List<EmpresaTicketsDTO> rankingEmpresas();

}
