package com.xochitepec.patrimonio.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.usuario.Usuario;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Convierte un JWT ya validado (firma y expiración) en la autenticación del usuario,
 * consultando la BD para obtener el rol y el área vigentes y rechazar usuarios desactivados.
 */
@Component
@RequiredArgsConstructor
public class JwtUsuarioConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioRepository usuarios;

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(Jwt jwt) {
        long id;
        try {
            id = Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Token inválido");
        }
        Usuario u = usuarios.findById(id).orElseThrow(() -> new BadCredentialsException("Token inválido"));
        if (!u.isActivo()) {
            throw new DisabledException("Usuario desactivado");
        }
        UsuarioActual actual = new UsuarioActual(u.getId(), u.getNombreCompleto(), u.getRol().getNombre(),
                u.getArea() == null ? null : u.getArea().getId());
        return new UsuarioAuthentication(actual, jwt);
    }
}
