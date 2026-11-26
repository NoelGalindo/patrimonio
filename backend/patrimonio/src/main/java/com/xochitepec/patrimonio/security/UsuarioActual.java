package com.xochitepec.patrimonio.security;

import com.xochitepec.patrimonio.usuario.RolNombre;

/** Identidad del usuario autenticado, cargada de la BD en cada petición. */
public record UsuarioActual(Long id, String nombreCompleto, RolNombre rol, Long areaId) {

    public boolean esUsuario() {
        return rol == RolNombre.USUARIO;
    }

    public boolean esPatrimonio() {
        return rol == RolNombre.PATRIMONIO;
    }

    public boolean esAdministrador() {
        return rol == RolNombre.ADMINISTRADOR;
    }
}
