package com.xochitepec.patrimonio.security;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Autenticación resultante de un JWT válido; el principal es el {@link UsuarioActual}. */
public class UsuarioAuthentication extends AbstractAuthenticationToken {

    private final UsuarioActual usuario;
    private final transient Jwt jwt;

    public UsuarioAuthentication(UsuarioActual usuario, Jwt jwt) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
        this.usuario = usuario;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public UsuarioActual getPrincipal() {
        return usuario;
    }

    @Override
    public String getName() {
        return String.valueOf(usuario.id());
    }
}
