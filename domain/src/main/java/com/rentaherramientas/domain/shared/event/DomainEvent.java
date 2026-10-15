package com.rentaherramientas.domain.shared.event;

import java.time.LocalDateTime;

/**
 * Contrato común de los eventos de dominio. Permite que la capa de aplicación
 * recoja y publique lo ocurrido en los agregados sin conocer su tipo concreto.
 */
public interface DomainEvent {

    LocalDateTime ocurridoEn();
}
