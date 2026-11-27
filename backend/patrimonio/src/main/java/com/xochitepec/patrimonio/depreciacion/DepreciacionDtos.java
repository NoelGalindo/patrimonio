package com.xochitepec.patrimonio.depreciacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DepreciacionDtos {

    private DepreciacionDtos() {
    }

    /** Referencia corta a la cuenta, incluida en la respuesta de cada bien. */
    public record CuentaRef(Long id, String clave, String concepto, Integer vidaUtilAnios, BigDecimal porcentajeAnual) {

        public static CuentaRef from(CuentaContable c) {
            return c == null ? null
                    : new CuentaRef(c.getId(), c.getClave(), c.getConcepto(), c.getVidaUtilAnios(),
                            porcentaje(c.getVidaUtilAnios()));
        }
    }

    public record CuentaContableResponse(Long id, String clave, String concepto, TipoBienContable tipoBien,
            Integer vidaUtilAnios, BigDecimal porcentajeAnual, boolean activo) {

        public static CuentaContableResponse from(CuentaContable c) {
            return new CuentaContableResponse(c.getId(), c.getClave(), c.getConcepto(), c.getTipoBien(),
                    c.getVidaUtilAnios(), porcentaje(c.getVidaUtilAnios()), c.isActivo());
        }
    }

    public record CuentaContableRequest(
            @NotBlank(message = "La clave es obligatoria") @Size(max = 20) String clave,
            @NotBlank(message = "El concepto es obligatorio") @Size(max = 200) String concepto,
            @NotNull(message = "El tipo de bien es obligatorio") TipoBienContable tipoBien,
            @Min(value = 1, message = "La vida útil debe ser de al menos 1 año")
            @Max(value = 100, message = "La vida útil no puede pasar de 100 años") Integer vidaUtilAnios,
            Boolean activo) {
    }

    /**
     * Depreciación de un bien a la fecha de consulta. Si no se puede calcular, los montos
     * vienen vacíos y {@code motivo} explica qué falta.
     */
    public record DepreciacionResumen(BigDecimal valorOriginal, BigDecimal porcentajeAnual,
            BigDecimal depreciacionAnual, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal depreciacionEjercicio,
            BigDecimal acumulada, BigDecimal valorLibros, Boolean totalmenteDepreciado, String motivo) {

        public static DepreciacionResumen sinCalculo(String motivo) {
            return new DepreciacionResumen(null, null, null, null, null, null, null, null, null, motivo);
        }
    }

    /** Totales de una cuenta en el reporte anual. */
    public record ReporteCuenta(String clave, String concepto, TipoBienContable tipoBien, Integer vidaUtilAnios,
            long bienes, BigDecimal valorOriginal, BigDecimal depreciacionEjercicio, BigDecimal acumulada,
            BigDecimal valorLibros) {
    }

    public record ReporteDepreciacion(int anio, ReglaInicioDepreciacion regla, List<ReporteCuenta> cuentas,
            ReporteCuenta total, long bienesSinCalculo) {
    }

    static BigDecimal porcentaje(Integer vidaUtil) {
        return vidaUtil == null || vidaUtil <= 0 ? null
                : BigDecimal.valueOf(100).divide(BigDecimal.valueOf(vidaUtil), 2, RoundingMode.HALF_UP);
    }
}
