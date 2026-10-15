package com.rentaherramientas.infrastructure.rental.adapters.in.scheduler;

import com.rentaherramientas.application.rental.usecase.VencerReservasUseCase;
import com.rentaherramientas.infrastructure.rental.config.RentalBeansConfig;
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

    public VencimientoReservasJob(VencerReservasUseCase vencerReservas) {
        this.vencerReservas = vencerReservas;
    }

    @Scheduled(cron = "${app.reservas.cron-vencimiento:0 5 0 * * *}", zone = RentalBeansConfig.ZONA_NEGOCIO)
    public void vencerReservas() {
        try {
            int vencidas = vencerReservas.vencer();
            log.info("Vencimiento de reservas: {} reserva(s) pasaron a VENCIDA", vencidas);
        } catch (RuntimeException e) {
            log.error("Falló el vencimiento de reservas", e);
        }
    }
}
