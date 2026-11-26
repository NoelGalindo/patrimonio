package com.xochitepec.patrimonio.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record DireccionRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 200) String nombre) {
    }

    public record DireccionResponse(Long id, String nombre) {
        public static DireccionResponse from(DireccionGeneral d) {
            return new DireccionResponse(d.getId(), d.getNombre());
        }
    }

    public record AreaRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 200) String nombre,
            @Size(max = 200) String nombreCargo,
            @NotNull(message = "La dirección general es obligatoria") Long direccionGeneralId) {
    }

    public record AreaResponse(Long id, String nombre, String nombreCargo, DireccionResponse direccionGeneral) {
        public static AreaResponse from(Area a) {
            if (a == null) {
                return null;
            }
            return new AreaResponse(a.getId(), a.getNombre(), a.getNombreCargo(),
                    DireccionResponse.from(a.getDireccionGeneral()));
        }
    }

    /** Referencia corta a un área, para incrustar en otros recursos. */
    public record AreaRef(Long id, String nombre, String direccionGeneral) {
        public static AreaRef from(Area a) {
            if (a == null) {
                return null;
            }
            return new AreaRef(a.getId(), a.getNombre(), a.getDireccionGeneral().getNombre());
        }
    }
}
