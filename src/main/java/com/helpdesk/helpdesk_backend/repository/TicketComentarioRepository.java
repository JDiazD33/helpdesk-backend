package com.helpdesk.helpdesk_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.TicketComentario;

@Repository
public interface TicketComentarioRepository extends JpaRepository<TicketComentario, Long> {

        // CONSULTAS DERIVADAS
        // Trazabilidad cronológica de comentarios de un ticket
        List<TicketComentario> findAllByTicketIdOrderByFechaEnvioAsc(Long ticketId);

        // Contador rápido de comentarios por ticket
        long countByTicketId(Long ticketId);

        // Eliminar todos los comentarios de un ticket
        void deleteByTicketId(Long ticketId);

        // CONSULTAS JPQL

        /**
         * Listar comentarios realizados por un usuario,
         * ordenados del más reciente al más antiguo.
         * (Consulta por usuario específico, no por tenant: no se excluye owner.)
         */
        @Query("""
                        SELECT c FROM TicketComentario c
                        JOIN FETCH c.usuario
                        JOIN FETCH c.ticket
                        WHERE c.usuario.id = :usuarioId
                        ORDER BY c.fechaEnvio DESC
                        """)
        List<TicketComentario> listarPorUsuario(@Param("usuarioId") Long usuarioId);

        /**
         * Obtener comentarios de un ticket en un rango de fechas.
         * Útil para auditoría o historial.
         */
        @Query("""
                        SELECT c FROM TicketComentario c
                        WHERE c.ticket.id = :ticketId
                        AND c.fechaEnvio BETWEEN :inicio AND :fin
                        ORDER BY c.fechaEnvio ASC
                        """)
        List<TicketComentario> buscarPorTicketYFechas(
                        @Param("ticketId") Long ticketId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        /**
         * Buscar comentarios por texto dentro del mensaje.
         * Búsqueda dinámica tipo buscador.
         */
        @Query("""
                        SELECT c FROM TicketComentario c
                        WHERE LOWER(c.mensaje) LIKE LOWER(CONCAT('%', :texto, '%'))
                        ORDER BY c.fechaEnvio DESC
                        """)
        List<TicketComentario> buscarPorTexto(@Param("texto") String texto);

        /**
         * Contar comentarios realizados por cada usuario dentro de un tenant,
         * excluyendo al ADMIN_OWNER. Si empresaId es NULL, es la vista global
         * del owner y no se aplica el filtro.
         */
        @Query("""
                        SELECT c.usuario.nombres, COUNT(c)
                        FROM TicketComentario c
                        JOIN c.ticket t
                        WHERE (:empresaId IS NULL OR t.empresa.id = :empresaId)
                        AND (:empresaId IS NULL OR c.usuario.rol.nombre <> :rolOwner)
                        GROUP BY c.usuario.nombres
                        ORDER BY COUNT(c) DESC
                        """)
        List<Object[]> rankingUsuariosComentariosExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolOwner") String rolOwner);

        /**
         * Obtener comentarios recientes de una empresa, excluyendo los del
         * ADMIN_OWNER. Aunque el owner raramente comenta, se aplica el filtro
         * para no contaminar el feed del tenant.
         */
        @Query("""
                        SELECT c FROM TicketComentario c
                        JOIN FETCH c.usuario u
                        JOIN FETCH c.ticket t
                        WHERE t.empresa.id = :empresaId
                        AND c.fechaEnvio >= :fecha
                        AND u.rol.nombre <> :rolOwner
                        ORDER BY c.fechaEnvio DESC
                        """)
        List<TicketComentario> comentariosRecientesEmpresaExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("fecha") LocalDateTime fecha,
                        @Param("rolOwner") String rolOwner);
}
