package com.rentaherramientas.domain.identity.event;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Se emite al registrar un usuario. Lleva el email (y no el id) porque en ese
 * momento el usuario aún no ha sido persistido y no tiene id asignado.
 */
public record UsuarioRegistrado(String email, Rol rol, LocalDateTime ocurridoEn) implements DomainEvent {
}
