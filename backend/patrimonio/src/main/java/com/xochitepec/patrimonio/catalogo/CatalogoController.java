package com.xochitepec.patrimonio.catalogo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRequest;
import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaResponse;
import com.xochitepec.patrimonio.catalogo.CatalogoDtos.DireccionRequest;
import com.xochitepec.patrimonio.catalogo.CatalogoDtos.DireccionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Lectura de catálogos para cualquier usuario autenticado (/api/catalogos) y
 * administración en /api/admin: direcciones solo ADMINISTRADOR; áreas ADMINISTRADOR y PATRIMONIO
 * (ver SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService service;

    @GetMapping("/api/catalogos/direcciones")
    public List<DireccionResponse> direcciones() {
        return service.listarDirecciones().stream().map(DireccionResponse::from).toList();
    }

    @GetMapping("/api/catalogos/areas")
    public List<AreaResponse> areas(@RequestParam(required = false) Long direccionGeneralId) {
        return service.listarAreas(direccionGeneralId).stream().map(AreaResponse::from).toList();
    }

    @PostMapping("/api/admin/direcciones")
    @ResponseStatus(HttpStatus.CREATED)
    public DireccionResponse crearDireccion(@Valid @RequestBody DireccionRequest req) {
        return DireccionResponse.from(service.guardarDireccion(null, req));
    }

    @PutMapping("/api/admin/direcciones/{id}")
    public DireccionResponse editarDireccion(@PathVariable Long id, @Valid @RequestBody DireccionRequest req) {
        return DireccionResponse.from(service.guardarDireccion(id, req));
    }

    @DeleteMapping("/api/admin/direcciones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarDireccion(@PathVariable Long id) {
        service.eliminarDireccion(id);
    }

    @PostMapping("/api/admin/areas")
    @ResponseStatus(HttpStatus.CREATED)
    public AreaResponse crearArea(@Valid @RequestBody AreaRequest req) {
        return AreaResponse.from(service.guardarArea(null, req));
    }

    @PutMapping("/api/admin/areas/{id}")
    public AreaResponse editarArea(@PathVariable Long id, @Valid @RequestBody AreaRequest req) {
        return AreaResponse.from(service.guardarArea(id, req));
    }

    @DeleteMapping("/api/admin/areas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarArea(@PathVariable Long id) {
        service.eliminarArea(id);
    }
}
