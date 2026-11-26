package com.xochitepec.patrimonio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.usuario.RolNombre;
import com.xochitepec.patrimonio.usuario.RolRepository;
import com.xochitepec.patrimonio.usuario.Usuario;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

/**
 * Crea el primer ADMINISTRADOR a partir de ADMIN_EMAIL / ADMIN_PASSWORD si todavía no existe ninguno.
 * Así ningún hash de contraseña queda escrito en las migraciones.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminSeeder(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
            @Value("${app.admin.email:}") String email, @Value("${app.admin.password:}") String password) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRolNombre(RolNombre.ADMINISTRADOR)) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("No existe ningún ADMINISTRADOR. Define ADMIN_EMAIL y ADMIN_PASSWORD para crear el primero.");
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 8 caracteres");
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setApellidoPaterno("del Sistema");
        admin.setCorreo(email.trim().toLowerCase());
        admin.setPasswordHash(encoder.encode(password));
        admin.setRol(roles.findByNombre(RolNombre.ADMINISTRADOR).orElseThrow());
        usuarios.save(admin);
        log.info("Administrador inicial creado: {}", admin.getCorreo());
    }
}
