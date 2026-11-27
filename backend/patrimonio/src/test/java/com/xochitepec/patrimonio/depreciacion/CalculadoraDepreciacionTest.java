package com.xochitepec.patrimonio.depreciacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.Depreciacion;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.Periodo;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.PeriodoAnual;

/** Pruebas puras (sin Docker) del cálculo de depreciación. */
class CalculadoraDepreciacionTest {

    private static final LocalDate COMPRA = LocalDate.of(2026, 8, 15);
    private static final BigDecimal COMPUTADORA = new BigDecimal("30000");

    private static Depreciacion computadora(ReglaInicioDepreciacion regla) {
        return CalculadoraDepreciacion.calcular(COMPUTADORA, COMPRA, 3, regla);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static BigDecimal sumaPeriodos(Depreciacion d) {
        return d.periodos().stream().map(Periodo::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void porcentajeAnualEs100EntreVidaUtil() {
        Depreciacion d = computadora(ReglaInicioDepreciacion.ANIO_SIGUIENTE);
        assertThat(d.porcentajeAnual()).isEqualByComparingTo("33.33");
        assertThat(d.depreciacionAnual()).isEqualByComparingTo("9999.67");
        assertThat(CalculadoraDepreciacion.calcular(COMPUTADORA, COMPRA, 10, ReglaInicioDepreciacion.ANIO_SIGUIENTE)
                .porcentajeAnual()).isEqualByComparingTo("10.00");
    }

    @Test
    void anioSiguienteEmpiezaEnEneroYQuedaEnUnPeso() {
        Depreciacion d = computadora(ReglaInicioDepreciacion.ANIO_SIGUIENTE);
        assertThat(d.fechaInicio()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(d.fechaFin()).isEqualTo(LocalDate.of(2029, 12, 31));
        assertThat(d.periodos()).extracting(Periodo::monto)
                .containsExactly(bd("9999.67"), bd("9999.67"), bd("9999.66"));
        assertThat(sumaPeriodos(d)).isEqualByComparingTo("29999");

        List<PeriodoAnual> cal = d.calendario();
        assertThat(cal).extracting(PeriodoAnual::anio).containsExactly(2027, 2028, 2029);
        assertThat(cal.get(2).valorLibros()).isEqualByComparingTo("1");

        // El año se aplica completo al cerrar (31 de diciembre)
        assertThat(d.acumuladaAl(LocalDate.of(2027, 12, 30))).isEqualByComparingTo("0");
        assertThat(d.acumuladaAl(LocalDate.of(2027, 12, 31))).isEqualByComparingTo("9999.67");
        assertThat(d.valorLibrosAl(LocalDate.of(2035, 1, 1))).isEqualByComparingTo("1");
        assertThat(d.totalmenteDepreciadoAl(LocalDate.of(2029, 12, 30))).isFalse();
        assertThat(d.totalmenteDepreciadoAl(LocalDate.of(2029, 12, 31))).isTrue();
    }

    @Test
    void anioDeAdquisicionCuentaCompleto() {
        Depreciacion d = computadora(ReglaInicioDepreciacion.ANIO_ADQUISICION);
        assertThat(d.fechaInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(d.calendario()).extracting(PeriodoAnual::anio).containsExactly(2026, 2027, 2028);
        assertThat(d.acumuladaAlCierre(2026)).isEqualByComparingTo("9999.67");
        assertThat(d.fechaFin()).isEqualTo(LocalDate.of(2028, 12, 31));
    }

    @Test
    void aniversarioCuentaAniosDesdeLaCompra() {
        Depreciacion d = computadora(ReglaInicioDepreciacion.ANIVERSARIO);
        assertThat(d.fechaInicio()).isEqualTo(COMPRA);
        assertThat(d.acumuladaAl(LocalDate.of(2027, 8, 14))).isEqualByComparingTo("0");
        assertThat(d.acumuladaAl(LocalDate.of(2027, 8, 15))).isEqualByComparingTo("9999.67");
        assertThat(d.fechaFin()).isEqualTo(LocalDate.of(2029, 8, 15));
        assertThat(d.calendario()).extracting(PeriodoAnual::anio).containsExactly(2027, 2028, 2029);
    }

    @Test
    void mesSiguienteEsProporcionalPorMes() {
        Depreciacion d = computadora(ReglaInicioDepreciacion.MES_SIGUIENTE);
        assertThat(d.fechaInicio()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(d.fechaFin()).isEqualTo(LocalDate.of(2029, 8, 31));
        assertThat(d.periodos()).hasSize(36);
        assertThat(sumaPeriodos(d)).isEqualByComparingTo("29999");

        // 2026: septiembre a diciembre (4 meses); 2029: enero a agosto (8 meses, con el ajuste de centavos)
        assertThat(d.delAnio(2026)).isEqualByComparingTo("3333.24");
        assertThat(d.delAnio(2027)).isEqualByComparingTo("9999.72");
        assertThat(d.delAnio(2029)).isEqualByComparingTo("6666.32");
        assertThat(d.acumuladaAl(LocalDate.of(2026, 9, 29))).isEqualByComparingTo("0");
        assertThat(d.acumuladaAl(LocalDate.of(2026, 9, 30))).isEqualByComparingTo("833.31");
        assertThat(d.calendario().get(d.calendario().size() - 1).valorLibros()).isEqualByComparingTo("1");
    }

    @Test
    void valorDeUnPesoOMenosNoSeDeprecia() {
        Depreciacion d = CalculadoraDepreciacion.calcular(BigDecimal.ONE, COMPRA, 5,
                ReglaInicioDepreciacion.ANIO_SIGUIENTE);
        assertThat(d.periodos()).isEmpty();
        assertThat(d.calendario()).isEmpty();
        assertThat(d.valorLibrosAl(LocalDate.of(2030, 1, 1))).isEqualByComparingTo("1");
        assertThat(d.totalmenteDepreciadoAl(COMPRA)).isTrue();

        Depreciacion cero = CalculadoraDepreciacion.calcular(new BigDecimal("0.50"), COMPRA, 5,
                ReglaInicioDepreciacion.MES_SIGUIENTE);
        assertThat(cero.valorLibrosAl(COMPRA)).isEqualByComparingTo("0.50");
    }

    @Test
    void cuentasLargasSumanExacto() {
        // Edificio de 30 años con un valor que no divide exacto
        Depreciacion d = CalculadoraDepreciacion.calcular(new BigDecimal("1234567.89"), LocalDate.of(2020, 2, 29),
                30, ReglaInicioDepreciacion.ANIVERSARIO);
        assertThat(sumaPeriodos(d)).isEqualByComparingTo("1234566.89");
        assertThat(d.fechaFin()).isEqualTo(LocalDate.of(2050, 2, 28));
    }
}
