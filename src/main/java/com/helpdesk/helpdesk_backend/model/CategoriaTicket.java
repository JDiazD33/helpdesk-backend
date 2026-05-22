package com.helpdesk.helpdesk_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "categoria_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * Entidad que agrupa los tipos de tickets (ej. Soporte Técnico, Facturación).
 * Cada categoría está vinculada exclusivamente a una empresa.
 */
public class CategoriaTicket {

   /* El valor del ID se autogenera en la BD */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @NotBlank(message = "El nombre de la categoría es obligatorio")
   @Size(max = 50, message = "El nombre no debe exceder 50 caracteres")
   @Column(nullable = false, length = 50)
   private String nombre;

   @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
   @Column(length = 250)
   private String descripcion;

   /*
    * Borrado lógico: Permite desactivar la categoría sin eliminarla físicamente,
    * asegurando que los tickets históricos no pierdan su referencia.
    */
   @Builder.Default
   @Column(nullable = false)
   private boolean activa = true;

   /*
    * Se agrego relacion con empresa, ya que cada categoria de ticket pertenece a
    * una empresa.
    * FetchType.LAZY: Optimiza la consulta al no cargar la empresa si no es
    * requerida.
    * 
    * @JsonIgnoreProperties: Evita que Jackson intente serializar atributos
    * técnicos
    * del Proxy de Hibernate (handler e initializer), evitando errores en la API.
    */
   @NotNull(message = "La empresa es obligatoria")
   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "empresa_id", nullable = false)
   @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
   private Empresa empresa;
}
