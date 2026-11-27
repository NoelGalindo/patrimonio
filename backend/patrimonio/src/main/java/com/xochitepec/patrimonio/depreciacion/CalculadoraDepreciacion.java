package com.xochitepec.patrimonio.depreciacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Depreciación en línea recta (Guía de Vida Útil del CONAC). Lógica pura, sin Spring.
 * <ul>
 * <li>Se deprecia el valor menos {@link #VALOR_DESECHO} ($1): el bien queda en el inventario con $1.</li>
 * <li>% anual = 100 / años de vida útil.</li>
 * <li>Los montos se redondean a centavos y el último periodo absorbe la diferencia, así que
 * la suma de todos los periodos es exactamente valor − $1.</li>
 * </ul>
 */
public final class CalculadoraDepreciacion {

    public static final BigDecimal VALOR_DESECHO = BigDecimal.ONE;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraDepreciacion() {
    }

    /** Un periodo de depreciación (año o mes); se considera aplicado al llegar su fecha de fin. */
    public record Periodo(LocalDate fin, BigDecimal monto) {
    }

    /** Fila del calendario: total de un año calendario. */
    public record PeriodoAnual(int anio, BigDecimal depreciacion, BigDecimal acumulada, BigDecimal valorLibros) {
    }

    /** Programa completo de depreciación de un bien. */
    public record Depreciacion(BigDecimal valor, int vidaUtilAnios, BigDecimal porcentajeAnual,
            BigDecimal depreciacionAnual, LocalDate fechaInicio, LocalDate fechaFin, List<Periodo> periodos) {

        /** Suma de los periodos ya terminados a la fecha dada. */
        public BigDecimal acumuladaAl(LocalDate fecha) {
            return suma(periodos.stream().filter(p -> !fecha.isBefore(p.fin())).toList());
        }

        /** Depreciación que corresponde al año calendario dado (aunque aún no termine). */
        public BigDecimal delAnio(int anio) {
            return suma(periodos.stream().filter(p -> p.fin().getYear() == anio).toList());
        }

        /** Depreciación acumulada al 31 de diciembre del año dado. */
        public BigDecimal acumuladaAlCierre(int anio) {
            return acumuladaAl(LocalDate.of(anio, 12, 31));
        }

        public BigDecimal valorLibrosAl(LocalDate fecha) {
            return valor.subtract(acumuladaAl(fecha));
        }

        public boolean totalmenteDepreciadoAl(LocalDate fecha) {
            return fechaFin == null || !fecha.isBefore(fechaFin);
        }

        public List<PeriodoAnual> calendario() {
            List<PeriodoAnual> filas = new ArrayList<>();
            if (periodos.isEmpty()) {
                return filas;
            }
            int desde = periodos.get(0).fin().getYear();
            int hasta = periodos.get(periodos.size() - 1).fin().getYear();
            for (int anio = desde; anio <= hasta; anio++) {
                BigDecimal acumulada = acumuladaAlCierre(anio);
                filas.add(new PeriodoAnual(anio, delAnio(anio), acumulada, valor.subtract(acumulada)));
            }
            return filas;
        }

        private static BigDecimal suma(List<Periodo> ps) {
            return ps.stream().map(Periodo::monto).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        }
    }

    public static Depreciacion calcular(BigDecimal valor, LocalDate fechaBase, int vidaUtilAnios,
            ReglaInicioDepreciacion regla) {
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(fechaBase, "fechaBase");
        Objects.requireNonNull(regla, "regla");
        if (vidaUtilAnios <= 0) {
            throw new IllegalArgumentException("La vida útil debe ser mayor a cero");
        }
        BigDecimal valor2 = valor.setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentaje = CIEN.divide(BigDecimal.valueOf(vidaUtilAnios), 2, RoundingMode.HALF_UP);
        BigDecimal depreciable = valor2.subtract(VALOR_DESECHO).max(BigDecimal.ZERO).setScale(2);
        BigDecimal anual = depreciable.divide(BigDecimal.valueOf(vidaUtilAnios), 2, RoundingMode.HALF_UP);
        LocalDate inicio = inicio(fechaBase, regla);

        if (depreciable.signum() == 0) {
            // Valor de $1 o menos: no hay nada que depreciar
            return new Depreciacion(valor2, vidaUtilAnios, porcentaje, anual, inicio, null, List.of());
        }

        List<LocalDate> fines = finesDePeriodo(fechaBase, vidaUtilAnios, regla);
        BigDecimal porPeriodo = depreciable.divide(BigDecimal.valueOf(fines.size()), 2, RoundingMode.HALF_UP);
        List<Periodo> periodos = new ArrayList<>(fines.size());
        BigDecimal aplicado = BigDecimal.ZERO;
        for (int i = 0; i < fines.size(); i++) {
            BigDecimal monto = i == fines.size() - 1 ? depreciable.subtract(aplicado) : porPeriodo;
            aplicado = aplicado.add(monto);
            periodos.add(new Periodo(fines.get(i), monto));
        }
        return new Depreciacion(valor2, vidaUtilAnios, porcentaje, anual, inicio, fines.get(fines.size() - 1),
                List.copyOf(periodos));
    }

    private static LocalDate inicio(LocalDate base, ReglaInicioDepreciacion regla) {
        return switch (regla) {
            case ANIO_SIGUIENTE -> LocalDate.of(base.getYear() + 1, 1, 1);
            case ANIO_ADQUISICION -> LocalDate.of(base.getYear(), 1, 1);
            case ANIVERSARIO -> base;
            case MES_SIGUIENTE -> YearMonth.from(base).plusMonths(1).atDay(1);
        };
    }

    /** Fecha en que termina cada periodo (año o mes) de la vida útil. */
    private static List<LocalDate> finesDePeriodo(LocalDate base, int anios, ReglaInicioDepreciacion regla) {
        List<LocalDate> fines = new ArrayList<>();
        switch (regla) {
            case ANIO_SIGUIENTE -> {
                for (int k = 1; k <= anios; k++) {
                    fines.add(LocalDate.of(base.getYear() + k, 12, 31));
                }
            }
            case ANIO_ADQUISICION -> {
                for (int k = 0; k < anios; k++) {
                    fines.add(LocalDate.of(base.getYear() + k, 12, 31));
                }
            }
            case ANIVERSARIO -> {
                for (int k = 1; k <= anios; k++) {
                    fines.add(base.plusYears(k));
                }
            }
            case MES_SIGUIENTE -> {
                YearMonth primero = YearMonth.from(base).plusMonths(1);
                for (int m = 0; m < anios * 12; m++) {
                    fines.add(primero.plusMonths(m).atEndOfMonth());
                }
            }
        }
        return fines;
    }
}
