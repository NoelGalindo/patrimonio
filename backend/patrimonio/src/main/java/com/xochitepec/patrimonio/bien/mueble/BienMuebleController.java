package com.xochitepec.patrimonio.bien.mueble;

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

import com.xochitepec.patrimonio.bien.mueble.BienMuebleDtos.BienMuebleRequest;
import com.xochitepec.patrimonio.bien.mueble.BienMuebleDtos.BienMuebleResponse;
import com.xochitepec.patrimonio.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bienes/muebles")
@RequiredArgsConstructor
public class BienMuebleController {

    private final BienMuebleService service;

    @GetMapping
    public PageResponse<BienMuebleResponse> listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "noControlInventario", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(service.listar(q, areaId, activo, pageable), BienMuebleResponse::from);
    }

    @GetMapping("/{id}")
    public BienMuebleResponse obtener(@PathVariable Long id) {
        return BienMuebleResponse.from(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienMuebleResponse crear(@Valid @RequestBody BienMuebleRequest req) {
        BienMueble b = new BienMueble();
        req.aplicar(b);
        return BienMuebleResponse.from(service.crear(b, req.areaId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATRIMONIO')")
    public BienMuebleResponse actualizar(@PathVariable Long id, @Valid @RequestBody BienMuebleRequest req) {
        return BienMuebleResponse.from(service.actualizar(id, req.areaId(), req::aplicar));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PATRIMONIO')")
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}
