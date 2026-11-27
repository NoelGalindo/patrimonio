package com.xochitepec.patrimonio.parametro;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xochitepec.patrimonio.common.ApiException;
import com.xochitepec.patrimonio.depreciacion.DepreciacionService;
import com.xochitepec.patrimonio.depreciacion.ReglaInicioDepreciacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Parámetros del sistema (firmantes del vale). Solo ADMINISTRADOR, por estar bajo /api/admin. */
@RestController
@RequestMapping("/api/admin/parametros")
@RequiredArgsConstructor
public class ParametroController {

    private final ParametroRepository repo;

    public record ParametroDto(@NotBlank String clave, @Size(max = 500) String valor, String descripcion) {
        static ParametroDto from(ParametroSistema p) {
            return new ParametroDto(p.getClave(), p.getValor(), p.getDescripcion());
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ParametroDto> listar() {
        return repo.findAllByOrderByClaveAsc().stream().map(ParametroDto::from).toList();
    }

    /** Actualiza el valor de parámetros existentes; no permite crear claves nuevas. */
    @PutMapping
    @Transactional
    public List<ParametroDto> actualizar(@Valid @RequestBody List<@Valid ParametroDto> cambios) {
        for (ParametroDto c : cambios) {
            ParametroSistema p = repo.findById(c.clave())
                    .orElseThrow(() -> ApiException.solicitudInvalida("Parámetro desconocido: " + c.clave()));
            String valor = c.valor() == null ? null : c.valor().trim();
            if (ReglaInicioDepreciacion.CLAVE_PARAMETRO.equals(c.clave())
                    && DepreciacionService.parsearRegla(valor).isEmpty()) {
                throw ApiException.solicitudInvalida("Regla de depreciación inválida: " + valor);
            }
            p.setValor(valor);
        }
        return listar();
    }
}
