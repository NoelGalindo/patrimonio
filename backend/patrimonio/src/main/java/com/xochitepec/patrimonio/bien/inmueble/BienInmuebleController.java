package com.xochitepec.patrimonio.bien.inmueble;

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
        return PageResponse.of(service.listar(q, areaId, activo, pageable), BienInmuebleResponse::from);
    }

    @GetMapping("/{id}")
    public BienInmuebleResponse obtener(@PathVariable Long id) {
        return BienInmuebleResponse.from(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienInmuebleResponse crear(@Valid @RequestBody BienInmuebleRequest req) {
        BienInmueble b = new BienInmueble();
        req.aplicar(b);
        return BienInmuebleResponse.from(service.crear(b, req.areaId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienInmuebleResponse actualizar(@PathVariable Long id, @Valid @RequestBody BienInmuebleRequest req) {
        return BienInmuebleResponse.from(service.actualizar(id, req.areaId(), req::aplicar));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}
