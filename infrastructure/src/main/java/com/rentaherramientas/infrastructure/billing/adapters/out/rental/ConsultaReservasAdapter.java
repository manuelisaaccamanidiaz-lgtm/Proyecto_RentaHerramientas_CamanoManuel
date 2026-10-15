package com.rentaherramientas.infrastructure.billing.adapters.out.rental;

import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador anticorrupción de billing: arma la ReservaFacturable con la reserva
 * (rental) y su herramienta (catalog). Solo aquí, en infraestructura, billing
 * "toca" esos BC.
 */
@Component
public class ConsultaReservasAdapter implements ConsultaReservas {

    private final ReservaRepository reservaRepository;
    private final HerramientaRepository herramientaRepository;

    public ConsultaReservasAdapter(ReservaRepository reservaRepository, HerramientaRepository herramientaRepository) {
        this.reservaRepository = reservaRepository;
        this.herramientaRepository = herramientaRepository;
    }

    @Override
    public Optional<ReservaFacturable> buscar(Long reservaId) {
        return reservaRepository.buscarPorId(reservaId).flatMap(reserva ->
                herramientaRepository.buscarPorId(reserva.getHerramientaId())
                        .map(herramienta -> facturable(reserva, herramienta)));
    }

    @Override
    public List<Long> reservaIdsDelCliente(Long clienteId) {
        return reservaRepository.idsPorCliente(clienteId);
    }

    @Override
    public List<Long> reservaIdsDelProveedor(Long proveedorId) {
        return reservaRepository.idsPorHerramientas(herramientaRepository.idsPorProveedor(proveedorId));
    }

    private static ReservaFacturable facturable(Reserva reserva, Herramienta herramienta) {
        return new ReservaFacturable(
                reserva.getId(),
                reserva.getClienteId(),
                herramienta.getProveedorId(),
                herramienta.getNombre(),
                reserva.getRango().inicio(),
                reserva.getRango().fin(),
                reserva.getRango().dias(),
                reserva.getPrecioTotal(),
                reserva.getEstado().name());
    }
}
