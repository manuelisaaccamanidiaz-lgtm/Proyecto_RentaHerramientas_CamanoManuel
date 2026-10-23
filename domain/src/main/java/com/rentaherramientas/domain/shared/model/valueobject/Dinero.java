package com.rentaherramientas.domain.shared.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * Value object monetario compartido (shared kernel) por catalog, rental y billing.
 * Une monto y moneda para que nunca se sumen pesos con dólares por accidente.
 * Se persiste aplanado como NUMERIC(12,2) + CHAR(3), de ahí los límites que valida.
 *
 * TÉCNICA AVANZADA (Java): BigDecimal para dinero
 *   Qué hace: `double` no representa exactamente valores como 0.1, así que nunca se usa para montos.
 *     `BigDecimal` es exacto y su escala (número de decimales) se fija a 2 para coincidir con NUMERIC(12,2).
 *   Cómo usarlo: crea montos con `new BigDecimal("1500.00")` (desde texto, no desde double) y opera con
 *     `add`, `multiply`, nunca con `+` o `*`.
 */
public record Dinero(BigDecimal monto, String moneda) {

    public static final String MONEDA_POR_DEFECTO = "COP";
    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");
    private static final int PRECISION_MAXIMA = 12;
    private static final int ESCALA = 2;
    public static final int DESCUENTO_MINIMO = 1;
    public static final int DESCUENTO_MAXIMO = 90;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    /**
     * Constructor compacto: valida moneda ISO 4217, signo, decimales y precisión, y fija la escala a 2.
     *
     * TÉCNICA AVANZADA (Java): `RoundingMode.UNNECESSARY`
     *   Qué hace: pide a `setScale` que falle si tuviera que redondear. Como antes se comprobó que hay como
     *     máximo 2 decimales, nunca redondea: garantiza que no se pierdan centavos en silencio.
     */
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

    /** Método fábrica equivalente al constructor; se lee mejor: `Dinero.de(monto, "COP")`. */
    public static Dinero de(BigDecimal monto, String moneda) {
        return new Dinero(monto, moneda);
    }

    /** Devuelve un monto de 0 en la moneda indicada; útil como valor inicial al acumular sumas. */
    public static Dinero cero(String moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }

    /** Indica si el monto es estrictamente mayor que cero. */
    public boolean esPositivo() {
        return monto.signum() > 0;
    }

    /** Indica si ambos montos están en la misma moneda (requisito para sumarlos). */
    public boolean mismaMonedaQue(Dinero otro) {
        return moneda.equals(otro.moneda);
    }

    /** Suma dos montos de la misma moneda y devuelve un Dinero nuevo (el original no cambia). */
    public Dinero sumar(Dinero otro) {
        if (!mismaMonedaQue(otro)) {
            throw new ValorInvalidoException(
                    "No se pueden sumar montos en monedas distintas: " + moneda + " y " + otro.moneda);
        }
        return new Dinero(monto.add(otro.monto), moneda);
    }

    /** Multiplica el monto por un entero no negativo, por ejemplo días de alquiler × tarifa diaria. */
    public Dinero multiplicar(int factor) {
        if (factor < 0) {
            throw new ValorInvalidoException("El factor de multiplicación no puede ser negativo");
        }
        return new Dinero(monto.multiply(BigDecimal.valueOf(factor)), moneda);
    }

    /**
     * Devuelve el monto con un descuento porcentual (1 a 90) aplicado, redondeado a centavos con HALF_UP.
     *
     * Nota: el redondeo se hace aquí, ANTES de construir el Dinero, porque el constructor usa
     *   `RoundingMode.UNNECESSARY` y fallaría con más de 2 decimales (33333.33 × 0.85 = 28333.3305).
     */
    public Dinero aplicarDescuento(int porcentaje) {
        if (porcentaje < DESCUENTO_MINIMO || porcentaje > DESCUENTO_MAXIMO) {
            throw new ValorInvalidoException("El descuento debe estar entre " + DESCUENTO_MINIMO + " y "
                    + DESCUENTO_MAXIMO + " %");
        }
        BigDecimal conDescuento = monto.multiply(BigDecimal.valueOf(100L - porcentaje))
                .divide(CIEN, ESCALA, RoundingMode.HALF_UP);
        return new Dinero(conDescuento, moneda);
    }
}
