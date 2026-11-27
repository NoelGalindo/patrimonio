package com.xochitepec.patrimonio.depreciacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.bien.BienBase;
import com.xochitepec.patrimonio.bien.inmueble.BienInmuebleRepository;
import com.xochitepec.patrimonio.bien.mueble.BienMuebleRepository;
import com.xochitepec.patrimonio.bien.vehiculo.VehiculoRepository;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.Depreciacion;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.ReporteCuenta;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.ReporteDepreciacion;

import lombok.RequiredArgsConstructor;

/** Reporte anual para Contabilidad: totales de depreciación por cuenta contable. */
@Service
@RequiredArgsConstructor
public class ReporteDepreciacionService {

    private final BienMuebleRepository muebles;
    private final BienInmuebleRepository inmuebles;
    private final VehiculoRepository vehiculos;
    private final DepreciacionService depreciacion;

    @Transactional(readOnly = true)
    public ReporteDepreciacion generar(int anio) {
        ReglaInicioDepreciacion regla = depreciacion.reglaActual();
        LocalDate cierre = LocalDate.of(anio, 12, 31);
        Map<String, Acumulador> porCuenta = new TreeMap<>();
        long sinCalculo = 0;

        List<BienBase> bienes = Stream.of(muebles.findAll(), inmuebles.findAll(), vehiculos.findAll())
                .flatMap(List::stream).map(BienBase.class::cast).filter(BienBase::isActivo).toList();
        for (BienBase b : bienes) {
            // Los bienes adquiridos después del cierre de ese año no entran
            if (b.fechaBaseDepreciacion() != null && b.fechaBaseDepreciacion().isAfter(cierre)) {
                continue;
            }
            if (DepreciacionService.motivoSinCalculo(b) != null) {
                sinCalculo++;
                continue;
            }
            Depreciacion d = depreciacion.programa(b, regla).orElseThrow();
            CuentaContable c = b.getCuentaContable();
            porCuenta.computeIfAbsent(c.getClave(), k -> new Acumulador(c)).sumar(d, anio);
        }

        List<ReporteCuenta> filas = new ArrayList<>();
        Acumulador total = new Acumulador(null);
        for (Acumulador a : porCuenta.values()) {
            filas.add(a.fila());
            total.sumar(a);
        }
        return new ReporteDepreciacion(anio, regla, filas, total.fila(), sinCalculo);
    }

    private static final class Acumulador {
        private final CuentaContable cuenta;
        private long bienes;
        private BigDecimal valor = BigDecimal.ZERO;
        private BigDecimal ejercicio = BigDecimal.ZERO;
        private BigDecimal acumulada = BigDecimal.ZERO;

        Acumulador(CuentaContable cuenta) {
            this.cuenta = cuenta;
        }

        void sumar(Depreciacion d, int anio) {
            bienes++;
            valor = valor.add(d.valor());
            ejercicio = ejercicio.add(d.delAnio(anio));
            acumulada = acumulada.add(d.acumuladaAlCierre(anio));
        }

        void sumar(Acumulador otro) {
            bienes += otro.bienes;
            valor = valor.add(otro.valor);
            ejercicio = ejercicio.add(otro.ejercicio);
            acumulada = acumulada.add(otro.acumulada);
        }

        ReporteCuenta fila() {
            return new ReporteCuenta(cuenta == null ? null : cuenta.getClave(),
                    cuenta == null ? "Total" : cuenta.getConcepto(), cuenta == null ? null : cuenta.getTipoBien(),
                    cuenta == null ? null : cuenta.getVidaUtilAnios(), bienes, valor.setScale(2),
                    ejercicio.setScale(2), acumulada.setScale(2), valor.subtract(acumulada).setScale(2));
        }
    }
}
