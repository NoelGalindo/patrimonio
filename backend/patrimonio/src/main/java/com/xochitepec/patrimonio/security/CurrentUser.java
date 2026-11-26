package com.xochitepec.patrimonio.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Acceso al usuario autenticado desde la capa de servicio. */
@Component
public class CurrentUser {

    public UsuarioActual get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioActual u) {
            return u;
        }
        throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
    }
}
