package com.rentaherramientas.domain.identity.model.valueobject;

/**
 * Roles posibles de un usuario. Es un conjunto cerrado de valores, por eso es un
 * enum; la base de datos replica la lista en ck_usuarios_rol.
 */
public enum Rol {
    ADMIN,
    PROVEEDOR,
    CLIENTE
}
