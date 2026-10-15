package com.rentaherramientas.infrastructure.shared.adapters.out.transaction;

import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Implementa la unidad de trabajo con una transacción de Spring. Los métodos
 * @Transactional de los adaptadores de persistencia (propagación REQUIRED) se
 * unen a ella, así que si cualquier paso lanza una excepción se deshace todo.
 */
@Component
public class UnidadDeTrabajoTransaccional implements UnidadDeTrabajo {

    private final TransactionTemplate transactionTemplate;

    public UnidadDeTrabajoTransaccional(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        return transactionTemplate.execute(status -> operacion.get());
    }
}
