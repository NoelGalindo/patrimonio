package com.xochitepec.patrimonio.bien.mueble;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BienMuebleDtos {

    private BienMuebleDtos() {
    }

    public record BienMuebleRequest(
            @NotBlank(message = "El número de control de inventario es obligatorio") @Size(max = 60) String noControlInventario,
            @NotBlank(message = "La descripción es obligatoria") @Size(max = 300) String descripcion,
            @Size(max = 120) String marca,
            @Size(max = 120) String modelo,
            @Size(max = 120) String serie,
            String especificaciones,
            @Size(max = 250) String ubicacion,
            @Size(max = 60) String estado,
            @Size(max = 200) String servidorPublico,
            @Size(max = 200) String cargo,
            LocalDate fechaResguardo,
            @Size(max = 60) String documentoSoporte,
            @Size(max = 60) String noFactura,
            @Size(max = 200) String expedidaPor,
            LocalDate fechaFactura,
            @DecimalMin(value = "0", message = "El costo no puede ser negativo") BigDecimal costo,
            @NotNull(message = "El área es obligatoria") Long areaId) {

        public void aplicar(BienMueble b) {
            b.setNoControlInventario(noControlInventario.trim());
            b.setDescripcion(descripcion.trim());
            b.setMarca(marca);
            b.setModelo(modelo);
            b.setSerie(serie);
            b.setEspecificaciones(especificaciones);
            b.setUbicacion(ubicacion);
            b.setEstado(estado);
            b.setServidorPublico(servidorPublico);
            b.setCargo(cargo);
            b.setFechaResguardo(fechaResguardo);
            b.setDocumentoSoporte(documentoSoporte);
            b.setNoFactura(noFactura);
            b.setExpedidaPor(expedidaPor);
            b.setFechaFactura(fechaFactura);
            b.setCosto(costo);
        }
    }

    public record BienMuebleResponse(Long id, String noControlInventario, String descripcion, String marca,
            String modelo, String serie, String especificaciones, String ubicacion, String estado,
            String servidorPublico, String cargo, LocalDate fechaResguardo, String documentoSoporte,
            String noFactura, String expedidaPor, LocalDate fechaFactura, BigDecimal costo, AreaRef area,
            boolean activo, Instant creadoEn, Instant actualizadoEn) {

        public static BienMuebleResponse from(BienMueble b) {
            return new BienMuebleResponse(b.getId(), b.getNoControlInventario(), b.getDescripcion(), b.getMarca(),
                    b.getModelo(), b.getSerie(), b.getEspecificaciones(), b.getUbicacion(), b.getEstado(),
                    b.getServidorPublico(), b.getCargo(), b.getFechaResguardo(), b.getDocumentoSoporte(),
                    b.getNoFactura(), b.getExpedidaPor(), b.getFechaFactura(), b.getCosto(),
                    AreaRef.from(b.getArea()), b.isActivo(), b.getCreadoEn(), b.getActualizadoEn());
        }
    }
}
