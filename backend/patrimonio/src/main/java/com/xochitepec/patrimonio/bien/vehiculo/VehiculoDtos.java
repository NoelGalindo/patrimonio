package com.xochitepec.patrimonio.bien.vehiculo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class VehiculoDtos {

    private VehiculoDtos() {
    }

    public record VehiculoRequest(
            @Size(max = 30) String numero,
            @Size(max = 60) String noEconomico,
            @NotBlank(message = "El número de inventario es obligatorio") @Size(max = 60) String noInventario,
            @Size(max = 60) String folioResguardo,
            LocalDate fechaRevision,
            @NotBlank(message = "La marca es obligatoria") @Size(max = 120) String marca,
            @Size(max = 120) String version,
            @Size(max = 60) String color,
            @Size(max = 60) String modelo,
            @Size(max = 60) String noSerie,
            @Size(max = 20) String cilindros,
            @Size(max = 60) String noMotor,
            @Size(max = 30) String placas,
            String documentacion,
            @Size(max = 200) String emisor,
            LocalDate fechaFactura,
            @Size(max = 60) String folioFactura,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal valorFactura,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal valorDepreciado,
            @Size(max = 150) String propiedad,
            @Size(max = 250) String condiciones,
            @NotNull(message = "El área designada es obligatoria") Long areaId) {

        public void aplicar(Vehiculo v) {
            v.setNumero(numero);
            v.setNoEconomico(noEconomico);
            v.setNoInventario(noInventario.trim());
            v.setFolioResguardo(folioResguardo);
            v.setFechaRevision(fechaRevision);
            v.setMarca(marca.trim());
            v.setVersion(version);
            v.setColor(color);
            v.setModelo(modelo);
            v.setNoSerie(noSerie == null || noSerie.isBlank() ? null : noSerie.trim());
            v.setCilindros(cilindros);
            v.setNoMotor(noMotor);
            v.setPlacas(placas);
            v.setDocumentacion(documentacion);
            v.setEmisor(emisor);
            v.setFechaFactura(fechaFactura);
            v.setFolioFactura(folioFactura);
            v.setValorFactura(valorFactura);
            v.setValorDepreciado(valorDepreciado);
            v.setPropiedad(propiedad);
            v.setCondiciones(condiciones);
        }
    }

    public record VehiculoResponse(Long id, String numero, String noEconomico, String noInventario,
            String folioResguardo, LocalDate fechaRevision, String marca, String version, String color,
            String modelo, String noSerie, String cilindros, String noMotor, String placas, String documentacion,
            String emisor, LocalDate fechaFactura, String folioFactura, BigDecimal valorFactura,
            BigDecimal valorDepreciado, String propiedad, String condiciones, AreaRef area, boolean activo,
            Instant creadoEn, Instant actualizadoEn) {

        public static VehiculoResponse from(Vehiculo v) {
            return new VehiculoResponse(v.getId(), v.getNumero(), v.getNoEconomico(), v.getNoInventario(),
                    v.getFolioResguardo(), v.getFechaRevision(), v.getMarca(), v.getVersion(), v.getColor(),
                    v.getModelo(), v.getNoSerie(), v.getCilindros(), v.getNoMotor(), v.getPlacas(),
                    v.getDocumentacion(), v.getEmisor(), v.getFechaFactura(), v.getFolioFactura(),
                    v.getValorFactura(), v.getValorDepreciado(), v.getPropiedad(), v.getCondiciones(),
                    AreaRef.from(v.getArea()), v.isActivo(), v.getCreadoEn(), v.getActualizadoEn());
        }
    }
}
