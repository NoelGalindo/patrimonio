package com.xochitepec.patrimonio.usuario;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.common.PageResponse;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.CambiarActivoRequest;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.CambiarPasswordRequest;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.CrearUsuarioRequest;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.EditarUsuarioRequest;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.UsuarioResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Gestión de usuarios. ADMINISTRADOR gestiona todos los roles; PATRIMONIO solo usuarios de área
 * (rol USUARIO), restricción que aplica UsuarioService.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioAdminController {

    private final UsuarioService service;

    @GetMapping
    public PageResponse<UsuarioResponse> listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) RolNombre rol,
            @RequestParam(required = false) Long areaId,
            @PageableDefault(size = 20, sort = "apellidoPaterno", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(service.buscar(q, rol, areaId, pageable), UsuarioResponse::from);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.from(service.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest req) {
        return UsuarioResponse.from(service.crear(req));
    }

    @PutMapping("/{id}")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody EditarUsuarioRequest req) {
        return UsuarioResponse.from(service.editar(id, req));
    }

    @PatchMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest req) {
        return UsuarioResponse.from(service.cambiarActivo(id, req.activo()));
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@PathVariable Long id, @Valid @RequestBody CambiarPasswordRequest req) {
        service.cambiarPassword(id, req.password());
    }
}
