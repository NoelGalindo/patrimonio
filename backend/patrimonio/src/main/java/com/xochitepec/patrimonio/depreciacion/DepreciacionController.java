package com.xochitepec.patrimonio.depreciacion;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.CuentaContableRequest;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.CuentaContableResponse;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.ReporteDepreciacion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo de cuentas contables (Guía de Vida Útil del CONAC) y reporte de depreciación.
 * Las rutas /api/admin/** son solo para ADMINISTRADOR (SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
public class DepreciacionController {

    private final CuentaContableRepository cuentas;
    private final ReporteDepreciacionService reportes;

    /** Cuentas que se pueden asignar a un bien del tipo dado (para el formulario de alta). */
    @GetMapping("/api/catalogos/cuentas-contables")
    @Transactional(readOnly = true)
    public List<CuentaContableResponse> asignables(@RequestParam TipoBienContable tipoBien) {
        return cuentas.findByTipoBienAndActivoTrueAndVidaUtilAniosNotNullOrderByClaveAsc(tipoBien).stream()
                .map(CuentaContableResponse::from).toList();
    }

    @GetMapping("/api/admin/cuentas-contables")
    @Transactional(readOnly = true)
    public List<CuentaContableResponse> listar() {
        return cuentas.findAllByOrderByClaveAsc().stream().map(CuentaContableResponse::from).toList();
    }

    @PostMapping("/api/admin/cuentas-contables")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CuentaContableResponse crear(@Valid @RequestBody CuentaContableRequest req) {
        String clave = req.clave().trim();
        if (cuentas.existsByClave(clave)) {
            throw ApiException.conflicto("Ya existe la cuenta " + clave);
        }
        CuentaContable c = new CuentaContable();
        c.setClave(clave);
        c.setTipoBien(req.tipoBien());
        aplicar(c, req);
        return CuentaContableResponse.from(cuentas.saveAndFlush(c));
    }

    /** Edita concepto, vida útil y estado. La clave y el tipo de bien no cambian (los bienes ya ligados dependen de ellos). */
    @PutMapping("/api/admin/cuentas-contables/{id}")
    @Transactional
    public CuentaContableResponse actualizar(@PathVariable Long id, @Valid @RequestBody CuentaContableRequest req) {
        CuentaContable c = cuentas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Cuenta contable"));
        if (!c.getClave().equals(req.clave().trim()) || c.getTipoBien() != req.tipoBien()) {
            throw ApiException.solicitudInvalida("La clave y el tipo de bien de una cuenta no se pueden cambiar");
        }
        aplicar(c, req);
        return CuentaContableResponse.from(cuentas.saveAndFlush(c));
    }

    @GetMapping("/api/reportes/depreciacion")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','PATRIMONIO')")
    public ReporteDepreciacion reporte(@RequestParam int anio) {
        if (anio < 1900 || anio > 2200) {
            throw ApiException.solicitudInvalida("Año inválido");
        }
        return reportes.generar(anio);
    }

    private static void aplicar(CuentaContable c, CuentaContableRequest req) {
        c.setConcepto(req.concepto().trim());
        c.setVidaUtilAnios(req.vidaUtilAnios());
        c.setActivo(req.activo() == null || req.activo());
    }
}
