package com.rentaherramientas.application.reporting.usecase;

import com.rentaherramientas.application.reporting.dto.ClienteFrecuenteResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaAlquiladaResponse;
import com.rentaherramientas.application.reporting.dto.IngresoMensualResponse;
import com.rentaherramientas.application.reporting.dto.InventarioResponse;
import com.rentaherramientas.application.reporting.dto.RentabilidadResponse;
import com.rentaherramientas.application.reporting.dto.ResumenGeneralResponse;
import com.rentaherramientas.application.reporting.dto.ResumenProveedorResponse;
import com.rentaherramientas.application.reporting.port.ConsultasReportes;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Valida y completa los parámetros de los reportes y delega en el puerto. Los
 * límites protegen a la BD de rankings o periodos desmesurados.
 */
public class ConsultarReportesService implements ConsultarReportesUseCase {

    static final int LIMITE_MAXIMO = 50;
    static final int MESES_POR_DEFECTO = 12;
    static final long DIAS_MAXIMOS_PERIODO = 5 * 366;

    private final ConsultasReportes consultasReportes;
    private final Clock clock;

    public ConsultarReportesService(ConsultasReportes consultasReportes, Clock clock) {
        this.consultasReportes = consultasReportes;
        this.clock = clock;
    }

    @Override
    public ResumenGeneralResponse resumenGeneral() {
        return consultasReportes.resumenGeneral();
    }

    @Override
    public List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta == null ? LocalDate.now(clock) : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1).minusMonths(MESES_POR_DEFECTO - 1) : desde;
        if (fin.isBefore(inicio)) {
            throw new ValorInvalidoException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > DIAS_MAXIMOS_PERIODO) {
            throw new ValorInvalidoException("El periodo del reporte no puede superar 5 años");
        }
        return consultasReportes.ingresosPorMes(inicio, fin);
    }

    @Override
    public List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite) {
        return consultasReportes.herramientasMasAlquiladas(validarLimite(limite));
    }

    @Override
    public List<ClienteFrecuenteResponse> clientesFrecuentes(int limite) {
        return consultasReportes.clientesFrecuentes(validarLimite(limite));
    }

    @Override
    public InventarioResponse inventario() {
        return consultasReportes.inventario();
    }

    @Override
    public RentabilidadResponse rentabilidad(int limite) {
        return consultasReportes.rentabilidad(validarLimite(limite));
    }

    @Override
    public ResumenProveedorResponse resumenProveedor(Long proveedorId) {
        return consultasReportes.resumenProveedor(Validaciones.requerido(proveedorId, "proveedorId"));
    }

    private static int validarLimite(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new ValorInvalidoException("El límite debe estar entre 1 y " + LIMITE_MAXIMO);
        }
        return limite;
    }
}
