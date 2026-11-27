package com.xochitepec.patrimonio.bien.inmueble;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.bien.inmueble.BienInmuebleDtos.BienInmuebleRequest;
import com.xochitepec.patrimonio.bien.inmueble.BienInmuebleDtos.BienInmuebleResponse;
import com.xochitepec.patrimonio.common.PageResponse;
import com.xochitepec.patrimonio.depreciacion.CalculadoraDepreciacion.PeriodoAnual;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bienes/inmuebles")
@RequiredArgsConstructor
public class BienInmuebleController {

    private final BienInmuebleService service;

    @GetMapping
    public PageResponse<BienInmuebleResponse> listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "noExpediente", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(service.listar(q, areaId, activo, pageable), service.conDepreciacion(BienInmuebleResponse::from));
    }

    @GetMapping("/{id}")
    public BienInmuebleResponse obtener(@PathVariable Long id) {
        return service.conDepreciacion(BienInmuebleResponse::from).apply(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienInmuebleResponse crear(@Valid @RequestBody BienInmuebleRequest req) {
        BienInmueble b = new BienInmueble();
        req.aplicar(b);
        return service.conDepreciacion(BienInmuebleResponse::from).apply(service.crear(b, req.areaId(), req.cuentaContableId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienInmuebleResponse actualizar(@PathVariable Long id, @Valid @RequestBody BienInmuebleRequest req) {
        return service.conDepreciacion(BienInmuebleResponse::from)
                .apply(service.actualizar(id, req.areaId(), req.cuentaContableId(), req::aplicar));
    }

    /** Calendario anual de depreciación (mismas reglas de visibilidad que el detalle). */
    @GetMapping("/{id}/depreciacion")
    public List<PeriodoAnual> depreciacion(@PathVariable Long id) {
        return service.calendario(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}
