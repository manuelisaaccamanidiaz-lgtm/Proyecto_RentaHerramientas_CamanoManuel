package com.rentaherramientas.domain.shared.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * Value object monetario compartido (shared kernel) por catalog, rental y billing.
 * Une monto y moneda para que nunca se sumen pesos con dólares por accidente.
 * Se persiste aplanado como NUMERIC(12,2) + CHAR(3), de ahí los límites que valida.
 */
public record Dinero(BigDecimal monto, String moneda) {

    public static final String MONEDA_POR_DEFECTO = "COP";
    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");
    private static final int PRECISION_MAXIMA = 12;
    private static final int ESCALA = 2;

    public Dinero {
        if (moneda == null || !ISO_4217.matcher(moneda).matches()) {
            throw new ValorInvalidoException("La moneda debe ser un código ISO 4217 de 3 letras mayúsculas");
        }
        if (monto == null) {
            throw new ValorInvalidoException("El monto es obligatorio");
        }
        if (monto.signum() < 0) {
            throw new ValorInvalidoException("El monto no puede ser negativo");
        }
        if (monto.stripTrailingZeros().scale() > ESCALA) {
            throw new ValorInvalidoException("El monto admite como máximo " + ESCALA + " decimales");
        }
        monto = monto.setScale(ESCALA, RoundingMode.UNNECESSARY);
        if (monto.precision() > PRECISION_MAXIMA) {
            throw new ValorInvalidoException("El monto excede la precisión permitida (12,2)");
        }
    }

    public static Dinero de(BigDecimal monto, String moneda) {
        return new Dinero(monto, moneda);
    }

    public static Dinero cero(String moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }

    public boolean esPositivo() {
        return monto.signum() > 0;
    }

    public boolean mismaMonedaQue(Dinero otro) {
        return moneda.equals(otro.moneda);
    }

    public Dinero sumar(Dinero otro) {
        if (!mismaMonedaQue(otro)) {
            throw new ValorInvalidoException(
                    "No se pueden sumar montos en monedas distintas: " + moneda + " y " + otro.moneda);
        }
        return new Dinero(monto.add(otro.monto), moneda);
    }

    public Dinero multiplicar(int factor) {
        if (factor < 0) {
            throw new ValorInvalidoException("El factor de multiplicación no puede ser negativo");
        }
        return new Dinero(monto.multiply(BigDecimal.valueOf(factor)), moneda);
    }
}
