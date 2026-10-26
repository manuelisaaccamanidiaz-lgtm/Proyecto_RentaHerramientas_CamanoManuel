package com.rentaherramientas.application.identity.exception;

/**
 * Se lanza cuando, al cambiar la contraseña, la contraseña actual enviada no
 * coincide con la guardada. Se traduce a 400 y no a 401: el usuario SÍ está
 * autenticado (su JWT es válido); lo que falla es un campo del cuerpo. Además,
 * el frontend interpreta cualquier 401 como sesión vencida y cierra la sesión.
 */
public class PasswordActualIncorrectaException extends RuntimeException {

    /** Mensaje fijo: no da pistas sobre la contraseña guardada. */
    public PasswordActualIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}
