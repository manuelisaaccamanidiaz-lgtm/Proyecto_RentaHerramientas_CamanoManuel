package com.rentaherramientas.application.notification.usecase;

import com.rentaherramientas.application.notification.port.ConsultaDestinatarios;
import com.rentaherramientas.application.notification.port.HerramientaNotificable;
import com.rentaherramientas.domain.billing.event.PagoAprobado;
import com.rentaherramientas.domain.notification.model.aggregate.Notificacion;
import com.rentaherramientas.domain.notification.port.repository.NotificacionRepository;
import com.rentaherramientas.domain.rental.event.ReservaAceptada;
import com.rentaherramientas.domain.rental.event.ReservaCancelada;
import com.rentaherramientas.domain.rental.event.ReservaRechazada;
import com.rentaherramientas.domain.rental.event.ReservaReprogramada;
import com.rentaherramientas.domain.rental.event.ReservaSolicitada;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Decide a quién avisar de cada evento y con qué texto:
 * ReservaSolicitada, ReservaCancelada y ReservaReprogramada → proveedor de la herramienta;
 * ReservaAceptada y ReservaRechazada → cliente; PagoAprobado → cliente (con el
 * número de factura) y proveedor. Si la herramienta o la reserva ya no existen
 * no hay a quién avisar y no se crea nada.
 */
public class NotificarEventoService implements NotificarEventoUseCase {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionRepository notificacionRepository;
    private final ConsultaDestinatarios consultaDestinatarios;
    private final Clock clock;

    /** Recibe sus dependencias por constructor; el bean lo crea NotificationBeansConfig. */
    public NotificarEventoService(NotificacionRepository notificacionRepository,
                                  ConsultaDestinatarios consultaDestinatarios, Clock clock) {
        this.notificacionRepository = notificacionRepository;
        this.consultaDestinatarios = consultaDestinatarios;
        this.clock = clock;
    }

    /**
     * Despacha según el tipo concreto del evento.
     *
     * TÉCNICA AVANZADA (Java 21): `switch` con patrones de tipo
     *   Qué hace: `case ReservaAceptada e ->` comprueba el tipo y, si coincide, deja el evento ya convertido
     *     en `e`; evita la cadena de `instanceof` + cast. `default` recoge los eventos que no avisan a nadie.
     */
    @Override
    public void notificar(DomainEvent evento) {
        switch (evento) {
            case ReservaSolicitada e -> conHerramienta(e.herramientaId(), h ->
                    crear(h.proveedorId(), "Nueva solicitud de reserva para " + h.nombre()));
            case ReservaAceptada e -> conHerramienta(e.herramientaId(), h ->
                    crear(e.clienteId(), "Tu reserva #" + e.reservaId() + " de " + h.nombre()
                            + " fue aceptada. Ya puedes pagarla."));
            case ReservaRechazada e -> conHerramienta(e.herramientaId(), h ->
                    crear(e.clienteId(), "Tu reserva #" + e.reservaId() + " de " + h.nombre() + " fue rechazada."));
            case ReservaCancelada e -> conHerramienta(e.herramientaId(), h ->
                    crear(h.proveedorId(), "El cliente canceló la reserva #" + e.reservaId() + " de "
                            + h.nombre() + "."));
            case ReservaReprogramada e -> conHerramienta(e.herramientaId(), h ->
                    crear(h.proveedorId(), "El cliente reprogramó la reserva #" + e.reservaId() + " de "
                            + h.nombre() + " al " + fechas(e.rangoNuevo()) + "."));
            case PagoAprobado e -> notificarPagoAprobado(e);
            default -> {
                // El resto de eventos (entregas, devoluciones, daños...) no genera avisos por ahora.
            }
        }
    }

    /**
     * El pago se busca por reserva y no por pagoId: un pago con TARJETA o PSE se aprueba antes de guardarse,
     * así que su evento lleva pagoId null.
     */
    private void notificarPagoAprobado(PagoAprobado evento) {
        consultaDestinatarios.reserva(evento.reservaId()).ifPresent(reserva -> {
            String factura = consultaDestinatarios.numeroFacturaDeReserva(evento.reservaId())
                    .map(numero -> "tu factura N.º " + numero + " está lista")
                    .orElse("tu factura está lista");
            crear(reserva.clienteId(), "Pago aprobado, " + factura + ".");
            crear(reserva.proveedorId(), "Se aprobó el pago de la reserva #" + reserva.reservaId() + " de "
                    + reserva.nombreHerramienta() + ".");
        });
    }

    /** Ejecuta la acción solo si la herramienta sigue existiendo. */
    private void conHerramienta(Long herramientaId, Consumer<HerramientaNotificable> accion) {
        consultaDestinatarios.herramienta(herramientaId).ifPresent(accion);
    }

    /** "01/11/2026 – 03/11/2026"; un solo día se muestra una vez. */
    private static String fechas(RangoFechas rango) {
        String inicio = rango.inicio().format(FORMATO_FECHA);
        return rango.dias() == 1 ? inicio : inicio + " – " + rango.fin().format(FORMATO_FECHA);
    }

    private void crear(Long usuarioId, String mensaje) {
        notificacionRepository.guardar(Notificacion.nueva(usuarioId, mensaje, LocalDateTime.now(clock)));
    }
}
