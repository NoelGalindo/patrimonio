package com.xochitepec.patrimonio.depreciacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.bien.BienBase;
import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.Depreciacion;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.PeriodoAnual;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.DepreciacionResumen;
import com.xochitepec.patrimonio.parametro.ParametroRepository;
import com.xochitepec.patrimonio.parametro.ParametroSistema;

import lombok.RequiredArgsConstructor;

/** Une los bienes con la calculadora: regla vigente, cuenta del bien y fecha de hoy. */
@Service
@RequiredArgsConstructor
public class DepreciacionService {

    public static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final ParametroRepository parametros;
    private final CuentaContableRepository cuentas;

    /** Regla configurada por el administrador; si el valor guardado no es válido, la predeterminada. */
    @Transactional(readOnly = true)
    public ReglaInicioDepreciacion reglaActual() {
        return parametros.findById(ReglaInicioDepreciacion.CLAVE_PARAMETRO)
                .map(ParametroSistema::getValor)
                .flatMap(DepreciacionService::parsearRegla)
                .orElse(ReglaInicioDepreciacion.PREDETERMINADA);
    }

    public static Optional<ReglaInicioDepreciacion> parsearRegla(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(ReglaInicioDepreciacion.valueOf(valor.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    /** Cuenta que se puede asignar a un bien del tipo dado (400 si no existe, no aplica o no tiene vida útil). */
    @Transactional(readOnly = true)
    public CuentaContable cuentaAsignable(Long cuentaId, TipoBienContable tipo) {
        if (cuentaId == null) {
            throw ApiException.solicitudInvalida("La cuenta contable es obligatoria");
        }
        CuentaContable c = cuentas.findById(cuentaId)
                .orElseThrow(() -> ApiException.solicitudInvalida("La cuenta contable no existe"));
        if (c.getTipoBien() != tipo) {
            throw ApiException.solicitudInvalida("La cuenta " + c.getClave() + " no corresponde a este tipo de bien");
        }
        if (!c.isActivo()) {
            throw ApiException.solicitudInvalida("La cuenta " + c.getClave() + " está desactivada");
        }
        if (c.getVidaUtilAnios() == null) {
            throw ApiException.solicitudInvalida(
                    "La cuenta " + c.getClave() + " no tiene vida útil definida; el administrador debe configurarla");
        }
        return c;
    }

    /** Programa de depreciación del bien, o vacío si le falta información. */
    public Optional<Depreciacion> programa(BienBase bien, ReglaInicioDepreciacion regla) {
        return motivoSinCalculo(bien) != null ? Optional.empty()
                : Optional.of(CalculadoraDepreciacion.calcular(bien.valorDepreciable(), bien.fechaBaseDepreciacion(),
                        bien.getCuentaContable().getVidaUtilAnios(), regla));
    }

    public DepreciacionResumen resumen(BienBase bien, ReglaInicioDepreciacion regla, LocalDate hoy) {
        String motivo = motivoSinCalculo(bien);
        if (motivo != null) {
            return DepreciacionResumen.sinCalculo(motivo);
        }
        Depreciacion d = programa(bien, regla).orElseThrow();
        BigDecimal acumulada = d.acumuladaAl(hoy);
        return new DepreciacionResumen(d.valor(), d.porcentajeAnual(), d.depreciacionAnual(), d.fechaInicio(),
                d.fechaFin(), d.delAnio(hoy.getYear()), acumulada, d.valor().subtract(acumulada),
                d.totalmenteDepreciadoAl(hoy), null);
    }

    public List<PeriodoAnual> calendario(BienBase bien) {
        return programa(bien, reglaActual()).map(Depreciacion::calendario).orElse(List.of());
    }

    /** Qué le falta al bien para calcular su depreciación (null si no le falta nada). */
    public static String motivoSinCalculo(BienBase bien) {
        CuentaContable c = bien.getCuentaContable();
        if (c == null) {
            return "Sin cuenta contable asignada";
        }
        if (c.getVidaUtilAnios() == null) {
            return "La cuenta " + c.getClave() + " no tiene vida útil definida";
        }
        if (bien.valorDepreciable() == null) {
            return "Falta " + bien.descripcionValorDepreciable();
        }
        if (bien.fechaBaseDepreciacion() == null) {
            return "Falta " + bien.descripcionFechaBase();
        }
        return null;
    }
}
