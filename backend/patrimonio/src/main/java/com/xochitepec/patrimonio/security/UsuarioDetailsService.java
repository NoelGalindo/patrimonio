package com.xochitepec.patrimonio.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.usuario.Usuario;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/** Usado solo por el login (correo + contraseña). */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) {
        Usuario u = usuarios.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getCorreo())
                .password(u.getPasswordHash())
                .roles(u.getRol().getNombre().name())
                .disabled(!u.isActivo())
                .build();
    }
}
