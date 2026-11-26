package com.xochitepec.patrimonio.traspaso;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.xochitepec.patrimonio.bien.mueble.BienMueble;
import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TraspasoDtos {

    private TraspasoDtos() {
    }

    public record CrearTraspasoRequest(
            @NotEmpty(message = "Selecciona al menos un bien") @Size(max = 100) List<@NotNull Long> bienIds,
            @NotNull(message = "El área destino es obligatoria") Long areaDestinoId,
            @Size(max = 100) String oficioPeticion,
            @Size(max = 2000) String motivo,
            @NotBlank(message = "Indica quién recibe el bien") @Size(max = 200) String recibeNombre,
            @Size(max = 200) String recibeCargo) {
    }

    public record ResolverTraspasoRequest(@Size(max = 2000) String comentario) {
    }

    public record UsuarioRef(Long id, String nombreCompleto) {
    }

    public record BienTraspasoResponse(Long bienId, String noInventario, String descripcion, String marca,
            String modelo, String serie, String estado, BigDecimal costo) {

        static BienTraspasoResponse from(SolicitudTraspasoDetalle d) {
            if (d.tieneSnapshot()) {
                return new BienTraspasoResponse(d.getBienMueble().getId(), d.getSnapNoInventario(),
                        d.getSnapDescripcion(), d.getSnapMarca(), d.getSnapModelo(), d.getSnapSerie(),
                        d.getSnapEstado(), d.getSnapCosto());
            }
            BienMueble b = d.getBienMueble();
            return new BienTraspasoResponse(b.getId(), b.getNoControlInventario(), b.getDescripcion(), b.getMarca(),
                    b.getModelo(), b.getSerie(), b.getEstado(), b.getCosto());
        }
    }

    public record TraspasoResponse(Long id, Integer folio, Integer anioFolio, String oficioPeticion,
            AreaRef areaOrigen, AreaRef areaDestino, UsuarioRef solicitante, String motivo, String recibeNombre,
            String recibeCargo, EstatusTraspaso estatus, Instant fechaSolicitud, UsuarioRef resueltoPor,
            Instant fechaResolucion, String comentarioResolucion, List<BienTraspasoResponse> bienes) {

        static TraspasoResponse from(SolicitudTraspaso s) {
            return new TraspasoResponse(s.getId(), s.getFolio(), s.getAnioFolio(), s.getOficioPeticion(),
                    AreaRef.from(s.getAreaOrigen()), AreaRef.from(s.getAreaDestino()),
                    new UsuarioRef(s.getSolicitante().getId(), s.getSolicitante().getNombreCompleto()),
                    s.getMotivo(), s.getRecibeNombre(), s.getRecibeCargo(), s.getEstatus(), s.getFechaSolicitud(),
                    s.getResueltoPor() == null ? null
                            : new UsuarioRef(s.getResueltoPor().getId(), s.getResueltoPor().getNombreCompleto()),
                    s.getFechaResolucion(), s.getComentarioResolucion(),
                    s.getDetalles().stream().map(BienTraspasoResponse::from).toList());
        }
    }
}
