package com.xochitepec.patrimonio.bien.vehiculo;

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

import com.xochitepec.patrimonio.bien.vehiculo.VehiculoDtos.VehiculoRequest;
import com.xochitepec.patrimonio.bien.vehiculo.VehiculoDtos.VehiculoResponse;
import com.xochitepec.patrimonio.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bienes/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService service;

    @GetMapping
    public PageResponse<VehiculoResponse> listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "noInventario", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(service.listar(q, areaId, activo, pageable), VehiculoResponse::from);
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return VehiculoResponse.from(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest req) {
        Vehiculo v = new Vehiculo();
        req.aplicar(v);
        return VehiculoResponse.from(service.crear(v, req.areaId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public VehiculoResponse actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequest req) {
        return VehiculoResponse.from(service.actualizar(id, req.areaId(), req::aplicar));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}
