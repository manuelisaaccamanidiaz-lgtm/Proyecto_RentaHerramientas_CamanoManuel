package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

import java.util.Collection;

/**
 * Se lanza cuando se pide una categoría por id, o se asignan a una herramienta
 * categorías, que no existen.
 */
public class CategoriaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción para una categoría concreta que no existe. */
    public CategoriaNoEncontradaException(Long id) {
        super("No existe la categoría " + id);
    }

    /** Crea la excepción cuando alguna de varias categorías pedidas no existe. */
    public CategoriaNoEncontradaException(Collection<Long> ids) {
        super("Alguna de las categorías indicadas no existe: " + ids);
    }
}
