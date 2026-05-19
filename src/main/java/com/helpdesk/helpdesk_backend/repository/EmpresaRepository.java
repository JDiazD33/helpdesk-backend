package com.helpdesk.helpdesk_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.helpdesk.helpdesk_backend.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    // Método para buscar una empresa por su RUC y que esté activa
    Optional<Empresa> findByRucAndActivoTrue(String ruc);

    // Método para buscar una empresa por su correo de contacto y que esté activa
    Optional<Empresa> findByCorreoContactoAndActivoTrue(String correoContacto);

    // Método para buscar una empresa por su ID y que esté activa
    Optional<Empresa> findByIdAndActivoTrue(Long id);

    // Método para buscar una empresa por su RUC sin importar si está activa o no
    Optional<Empresa> findByRuc(String ruc);
}
