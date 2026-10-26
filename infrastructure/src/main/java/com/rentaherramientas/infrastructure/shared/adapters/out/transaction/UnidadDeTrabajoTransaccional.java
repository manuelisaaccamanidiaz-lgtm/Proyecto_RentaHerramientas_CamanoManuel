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
 *
 * TÉCNICA AVANZADA (Spring): transacciones programáticas con `TransactionTemplate`
 *   Qué hace: es la alternativa a `@Transactional` cuando la transacción se decide en tiempo de ejecución o
 *     en código que no es un bean de Spring (como los servicios de application). `execute` abre la
 *     transacción, ejecuta la lambda y hace commit; si la lambda lanza una `RuntimeException`, hace rollback.
 *   Cómo usarlo: `transactionTemplate.execute(status -> { ...; return resultado; })`.
 */
@Component
public class UnidadDeTrabajoTransaccional implements UnidadDeTrabajo {

    private final TransactionTemplate transactionTemplate;

    /** Recibe el gestor de transacciones de JPA que configura Spring Boot y crea la plantilla. */
    public UnidadDeTrabajoTransaccional(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** Ejecuta la operación dentro de una transacción y devuelve su resultado. */
    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        return transactionTemplate.execute(status -> operacion.get());
    }
}
