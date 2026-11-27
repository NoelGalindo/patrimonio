package com.xochitepec.patrimonio.bien.inmueble;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.xochitepec.patrimonio.catalogo.CatalogoDtos.AreaRef;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.CuentaRef;
import com.xochitepec.patrimonio.depreciacion.DepreciacionDtos.DepreciacionResumen;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BienInmuebleDtos {

    private BienInmuebleDtos() {
    }

    public record BienInmuebleRequest(
            @NotBlank(message = "El número de expediente es obligatorio") @Size(max = 60) String noExpediente,
            LocalDate fechaRevision,
            @Size(max = 150) String colonia,
            @Size(max = 200) String calle,
            @NotBlank(message = "La denominación es obligatoria") @Size(max = 300) String denominacion,
            @Size(max = 150) String tipoUso,
            String medidasColindancias,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitud,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitud,
            @Size(max = 300) String propietarios,
            LocalDate fechaAdquisicion,
            @Size(max = 60) String fojas,
            String observaciones,
            String documentacion,
            @Size(max = 60) String claveCatastral,
            LocalDate fechaUltimoAvaluo,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal valorTerreno,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal valorConstruccion,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal valorTotal,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal superficieConstruccion,
            @DecimalMin(value = "0", message = "No puede ser negativo") BigDecimal superficiePredio,
            @NotNull(message = "El área es obligatoria") Long areaId,
            @NotNull(message = "La cuenta contable es obligatoria") Long cuentaContableId) {

        public void aplicar(BienInmueble b) {
            b.setNoExpediente(noExpediente.trim());
            b.setFechaRevision(fechaRevision);
            b.setColonia(colonia);
            b.setCalle(calle);
            b.setDenominacion(denominacion.trim());
            b.setTipoUso(tipoUso);
            b.setMedidasColindancias(medidasColindancias);
            b.setLatitud(latitud);
            b.setLongitud(longitud);
            b.setPropietarios(propietarios);
            b.setFechaAdquisicion(fechaAdquisicion);
            b.setFojas(fojas);
            b.setObservaciones(observaciones);
            b.setDocumentacion(documentacion);
            b.setClaveCatastral(claveCatastral);
            b.setFechaUltimoAvaluo(fechaUltimoAvaluo);
            b.setValorTerreno(valorTerreno);
            b.setValorConstruccion(valorConstruccion);
            // Si no se captura el total, se calcula como terreno + construcción
            BigDecimal total = valorTotal;
            if (total == null && (valorTerreno != null || valorConstruccion != null)) {
                total = (valorTerreno == null ? BigDecimal.ZERO : valorTerreno)
                        .add(valorConstruccion == null ? BigDecimal.ZERO : valorConstruccion);
            }
            b.setValorTotal(total);
            b.setSuperficieConstruccion(superficieConstruccion);
            b.setSuperficiePredio(superficiePredio);
        }
    }

    public record BienInmuebleResponse(Long id, String noExpediente, LocalDate fechaRevision, String colonia,
            String calle, String denominacion, String tipoUso, String medidasColindancias, BigDecimal latitud,
            BigDecimal longitud, String propietarios, LocalDate fechaAdquisicion, String fojas,
            String observaciones, String documentacion, String claveCatastral, LocalDate fechaUltimoAvaluo,
            BigDecimal valorTerreno, BigDecimal valorConstruccion, BigDecimal valorTotal,
            BigDecimal superficieConstruccion, BigDecimal superficiePredio, AreaRef area, CuentaRef cuentaContable,
            DepreciacionResumen depreciacion, boolean activo,
            Instant creadoEn, Instant actualizadoEn) {

        public static BienInmuebleResponse from(BienInmueble b, DepreciacionResumen depreciacion) {
            return new BienInmuebleResponse(b.getId(), b.getNoExpediente(), b.getFechaRevision(), b.getColonia(),
                    b.getCalle(), b.getDenominacion(), b.getTipoUso(), b.getMedidasColindancias(), b.getLatitud(),
                    b.getLongitud(), b.getPropietarios(), b.getFechaAdquisicion(), b.getFojas(),
                    b.getObservaciones(), b.getDocumentacion(), b.getClaveCatastral(), b.getFechaUltimoAvaluo(),
                    b.getValorTerreno(), b.getValorConstruccion(), b.getValorTotal(), b.getSuperficieConstruccion(),
                    b.getSuperficiePredio(), AreaRef.from(b.getArea()),
                    CuentaRef.from(b.getCuentaContable()), depreciacion, b.isActivo(), b.getCreadoEn(),
                    b.getActualizadoEn());
        }
    }
}
