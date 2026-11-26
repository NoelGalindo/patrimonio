package com.xochitepec.patrimonio.usuario;

import java.time.Instant;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record CrearUsuarioRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "El apellido paterno es obligatorio") @Size(max = 100) String apellidoPaterno,
            @Size(max = 100) String apellidoMaterno,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") @Size(max = 150) String correo,
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
            @NotNull(message = "El rol es obligatorio") RolNombre rol,
            Long areaId) {
    }

    public record EditarUsuarioRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "El apellido paterno es obligatorio") @Size(max = 100) String apellidoPaterno,
            @Size(max = 100) String apellidoMaterno,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") @Size(max = 150) String correo,
            @NotNull(message = "El rol es obligatorio") RolNombre rol,
            Long areaId) {
    }

    public record CambiarPasswordRequest(
            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {
    }

    public record CambiarActivoRequest(@NotNull(message = "Indica si el usuario está activo") Boolean activo) {
    }

    public record UsuarioResponse(Long id, String nombre, String apellidoPaterno, String apellidoMaterno,
            String nombreCompleto, String correo, RolNombre rol, AreaRef area, boolean activo, Instant creadoEn) {

        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellidoPaterno(), u.getApellidoMaterno(),
                    u.getNombreCompleto(), u.getCorreo(), u.getRol().getNombre(), AreaRef.from(u.getArea()),
                    u.isActivo(), u.getCreadoEn());
        }
    }
}
