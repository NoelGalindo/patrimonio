package com.xochitepec.patrimonio.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.catalogo.AreaRepository;
import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.security.CurrentUser;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.CrearUsuarioRequest;
import com.xochitepec.patrimonio.usuario.UsuarioDtos.EditarUsuarioRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final AreaRepository areas;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public Page<Usuario> buscar(String q, RolNombre rol, Long areaId, Pageable pageable) {
        String patron = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        // Patrimonio solo administra usuarios de área: nunca ve administradores ni a sus pares
        RolNombre filtroRol = currentUser.get().esPatrimonio() ? RolNombre.USUARIO : rol;
        return usuarios.buscar(patron, filtroRol, areaId, pageable);
    }

    /**
     * Usuario visible para quien hace la petición. Para Patrimonio, cualquier usuario que no sea
     * de rol USUARIO se reporta como inexistente (404).
     */
    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario"));
        if (currentUser.get().esPatrimonio() && u.getRol().getNombre() != RolNombre.USUARIO) {
            throw ApiException.noEncontrado("Usuario");
        }
        return u;
    }

    @Transactional
    public Usuario crear(CrearUsuarioRequest req) {
        exigirRolPermitido(req.rol());
        String correo = req.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw ApiException.conflicto("Ya existe un usuario con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setApellidoPaterno(req.apellidoPaterno().trim());
        u.setApellidoMaterno(blankToNull(req.apellidoMaterno()));
        u.setCorreo(correo);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRol(rol(req.rol()));
        u.setArea(resolverArea(req.rol(), req.areaId()));
        return usuarios.save(u);
    }

    @Transactional
    public Usuario editar(Long id, EditarUsuarioRequest req) {
        Usuario u = obtener(id);
        exigirRolPermitido(req.rol());
        String correo = req.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw ApiException.conflicto("Ya existe un usuario con ese correo");
        }
        if (id.equals(currentUser.get().id()) && req.rol() != RolNombre.ADMINISTRADOR) {
            throw ApiException.solicitudInvalida("No puedes quitarte a ti mismo el rol de administrador");
        }
        u.setNombre(req.nombre().trim());
        u.setApellidoPaterno(req.apellidoPaterno().trim());
        u.setApellidoMaterno(blankToNull(req.apellidoMaterno()));
        u.setCorreo(correo);
        u.setRol(rol(req.rol()));
        u.setArea(resolverArea(req.rol(), req.areaId()));
        return u;
    }

    @Transactional
    public Usuario cambiarActivo(Long id, boolean activo) {
        if (!activo && id.equals(currentUser.get().id())) {
            throw ApiException.solicitudInvalida("No puedes desactivar tu propio usuario");
        }
        Usuario u = obtener(id);
        u.setActivo(activo);
        return u;
    }

    @Transactional
    public void cambiarPassword(Long id, String password) {
        obtener(id).setPasswordHash(passwordEncoder.encode(password));
    }

    /** Patrimonio solo puede asignar el rol USUARIO; el administrador, cualquiera. */
    private void exigirRolPermitido(RolNombre rol) {
        if (currentUser.get().esPatrimonio() && rol != RolNombre.USUARIO) {
            throw ApiException.prohibido("Patrimonio solo puede registrar usuarios de área (rol USUARIO)");
        }
    }

    private Rol rol(RolNombre nombre) {
        return roles.findByNombre(nombre)
                .orElseThrow(() -> ApiException.solicitudInvalida("Rol inexistente: " + nombre));
    }

    /** El rol USUARIO requiere área: es lo que delimita qué bienes puede ver. */
    private Area resolverArea(RolNombre rol, Long areaId) {
        if (areaId == null) {
            if (rol == RolNombre.USUARIO) {
                throw ApiException.solicitudInvalida("Los usuarios con rol USUARIO deben tener un área asignada");
            }
            return null;
        }
        return areas.findById(areaId).orElseThrow(() -> ApiException.solicitudInvalida("El área no existe"));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
