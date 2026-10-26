package com.rentaherramientas.infrastructure.rental.adapters.in.scheduler;

import com.rentaherramientas.application.rental.usecase.VencerReservasUseCase;
import com.rentaherramientas.infrastructure.shared.config.RelojConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada programado: cada día (por defecto a las 00:05 hora de
 * Bogotá, configurable con app.reservas.cron-vencimiento) vence las reservas
 * que ya no pueden cumplirse. Un fallo se registra y no detiene el planificador:
 * la siguiente ejecución vuelve a intentarlo.
 */
@Component
public class VencimientoReservasJob {

    private static final Logger log = LoggerFactory.getLogger(VencimientoReservasJob.class);

    private final VencerReservasUseCase vencerReservas;

    /** Recibe el caso de uso por constructor (inyección de dependencias de Spring). */
    public VencimientoReservasJob(VencerReservasUseCase vencerReservas) {
        this.vencerReservas = vencerReservas;
    }

    /**
     * Ejecuta el vencimiento y registra cuántas reservas pasaron a VENCIDA.
     *
     * TÉCNICA AVANZADA (Spring): `@Scheduled(cron = ...)`
     *   Qué hace: Spring llama a este método automáticamente según la expresión cron (segundo, minuto, hora,
     *     día, mes, día de la semana): `"0 5 0 * * *"` es todos los días a las 00:05. `zone` fija la zona
     *     horaria y `${clave:valor}` permite cambiar el horario desde la configuración. Requiere
     *     `@EnableScheduling` (está en RentalBeansConfig).
     *   Cómo usarlo: anota un método `void` sin parámetros de un bean con `@Scheduled(cron = "...")` o
     *     `@Scheduled(fixedRate = 60000)`.
     */
    @Scheduled(cron = "${app.reservas.cron-vencimiento:0 5 0 * * *}", zone = RelojConfig.ZONA_NEGOCIO)
    public void vencerReservas() {
        try {
            int vencidas = vencerReservas.vencer();
            log.info("Vencimiento de reservas: {} reserva(s) pasaron a VENCIDA", vencidas);
        } catch (RuntimeException e) {
            log.error("Falló el vencimiento de reservas", e);
        }
    }
}
