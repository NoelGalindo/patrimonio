package com.xochitepec.patrimonio.auth;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;
import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.security.CurrentUser;
import com.xochitepec.patrimonio.security.JwtService;
import com.xochitepec.patrimonio.usuario.RolNombre;
import com.xochitepec.patrimonio.usuario.Usuario;
import com.xochitepec.patrimonio.usuario.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final JwtService jwtService;
    private final CurrentUser currentUser;

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio") String correo,
            @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    public record PerfilResponse(Long id, String nombreCompleto, String correo, RolNombre rol, AreaRef area) {
        static PerfilResponse from(Usuario u) {
            return new PerfilResponse(u.getId(), u.getNombreCompleto(), u.getCorreo(), u.getRol().getNombre(),
                    AreaRef.from(u.getArea()));
        }
    }

    public record LoginResponse(String token, Instant expira, PerfilResponse usuario) {
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        // Lanza BadCredentialsException / DisabledException -> 401 (GlobalExceptionHandler)
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.correo().trim(), req.password()));
        Usuario u = usuarios.findByCorreoIgnoreCase(req.correo().trim()).orElseThrow();
        JwtService.Token token = jwtService.emitir(u);
        return new LoginResponse(token.valor(), token.expira(), PerfilResponse.from(u));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public PerfilResponse me() {
        return usuarios.findById(currentUser.get().id()).map(PerfilResponse::from)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario"));
    }
}
