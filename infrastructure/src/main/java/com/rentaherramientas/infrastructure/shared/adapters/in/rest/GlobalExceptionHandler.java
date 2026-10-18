package com.rentaherramientas.infrastructure.shared.adapters.in.rest;

import com.rentaherramientas.application.identity.exception.CredencialesInvalidasException;
import com.rentaherramientas.application.identity.exception.EmailYaRegistradoException;
import com.rentaherramientas.application.identity.exception.RolNoPermitidoException;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.exception.ConflictoException;
import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.identity.exception.UsuarioInactivoException;
import com.rentaherramientas.domain.shared.exception.DomainException;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas ProblemDetail (RFC 9457). Hereda de
 * ResponseEntityExceptionHandler para que los errores propios de Spring MVC
 * (JSON mal formado, parámetro de tipo incorrecto, multipart sin el campo,
 * método no soportado...) también respondan 4xx con ProblemDetail en vez de
 * caer en el 500 genérico. Spring elige el handler de la excepción más
 * específica, por eso las subclases de DomainException tienen el suyo.
 *
 * TÉCNICA AVANZADA (Spring): `@RestControllerAdvice` + `@ExceptionHandler`
 *   Qué hace: un único bean intercepta las excepciones que lanzan TODOS los controllers y las convierte en
 *     respuestas HTTP. Así ningún controller necesita try/catch.
 *   Cómo usarlo: añade un método `@ExceptionHandler(MiExcepcion.class)` que devuelva un `ProblemDetail` (o
 *     `ResponseEntity`); Spring elige el más específico.
 *
 * TÉCNICA AVANZADA (Spring 6): `ProblemDetail` (RFC 9457)
 *   Qué hace: formato estándar de error JSON con `type`, `title`, `status`, `detail` y propiedades extra con
 *     `setProperty`. El frontend puede leer siempre los errores de la misma forma.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 400 con el detalle por campo en la propiedad "errores".
     *
     * TÉCNICA AVANZADA (Spring): sobrescribir un handler de Spring MVC
     *   Qué hace: este método se ejecuta cuando falla `@Valid` en un `@RequestBody`. `putIfAbsent` conserva
     *     solo el primer error de cada campo.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La petición tiene campos inválidos");
        problema.setTitle("Datos inválidos");
        problema.setProperty("errores", errores);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** 413: el archivo supera spring.servlet.multipart.max-file-size. */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        ProblemDetail problema = problema(HttpStatus.CONTENT_TOO_LARGE, "Archivo demasiado grande",
                "El archivo supera el tamaño máximo permitido");
        return handleExceptionInternal(ex, problema, headers, HttpStatus.CONTENT_TOO_LARGE, request);
    }

    /** 400: un value object o atributo del dominio recibió un valor con formato o rango inválido. */
    @ExceptionHandler(ValorInvalidoException.class)
    ProblemDetail valorInvalido(ValorInvalidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Valor inválido", ex.getMessage());
    }

    /** 400: el registro público pidió un rol distinto de CLIENTE o PROVEEDOR. */
    @ExceptionHandler(RolNoPermitidoException.class)
    ProblemDetail rolNoPermitido(RolNoPermitidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Rol no permitido", ex.getMessage());
    }

    /** 401: email o contraseña incorrectos en el login. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    ProblemDetail credencialesInvalidas(CredencialesInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    /** 403: el usuario está desactivado. */
    @ExceptionHandler(UsuarioInactivoException.class)
    ProblemDetail usuarioInactivo(UsuarioInactivoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Usuario inactivo", ex.getMessage());
    }

    /** 403: el usuario está autenticado pero no puede actuar sobre ese recurso (no es su dueño). */
    @ExceptionHandler(AccesoDenegadoException.class)
    ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    /** Cualquier *NoEncontradoException de cualquier BC. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    /** 409: ya existe un usuario con ese email. */
    @ExceptionHandler(EmailYaRegistradoException.class)
    ProblemDetail emailYaRegistrado(EmailYaRegistradoException ex) {
        return problema(HttpStatus.CONFLICT, "Email ya registrado", ex.getMessage());
    }

    /** Resto de conflictos de aplicación (nombre de categoría repetido, auto-desactivación...). */
    @ExceptionHandler(ConflictoException.class)
    ProblemDetail conflicto(ConflictoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    /**
     * Restricción de la BD violada (UNIQUE, FK). El mensaje real revelaría el esquema: se registra y no se expone.
     *
     * Nota: `getMostSpecificCause()` recorre la cadena de causas y devuelve la excepción original del driver
     *   de PostgreSQL, que contiene el nombre de la restricción violada.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridadDatos(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación entra en conflicto con datos existentes (valor duplicado o recurso en uso)");
    }

    /** Resto de reglas de negocio del dominio. */
    @ExceptionHandler(DomainException.class)
    ProblemDetail reglaDeNegocio(DomainException ex) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio incumplida", ex.getMessage());
    }

    /**
     * 500: red de seguridad para cualquier otra excepción. Registra la traza completa en el log y
     * responde un mensaje genérico, sin filtrar detalles internos al cliente.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Inténtalo de nuevo más tarde.");
    }

    /** Construye un ProblemDetail con estado, título y detalle. */
    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
