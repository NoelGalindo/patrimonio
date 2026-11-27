package com.xochitepec.patrimonio.bien.inmueble;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.xochitepec.patrimonio.bien.BienBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Relación de bienes inmuebles. */
@Entity
@Table(name = "bien_inmueble")
@Getter
@Setter
@NoArgsConstructor
public class BienInmueble extends BienBase {

    @Column(name = "no_expediente", nullable = false, unique = true, length = 60)
    private String noExpediente;

    @Column(name = "fecha_revision")
    private LocalDate fechaRevision;

    @Column(length = 150)
    private String colonia;

    @Column(length = 200)
    private String calle;

    @Column(nullable = false, length = 300)
    private String denominacion;

    @Column(name = "tipo_uso", length = 150)
    private String tipoUso;

    @Column(name = "medidas_colindancias", columnDefinition = "text")
    private String medidasColindancias;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(length = 300)
    private String propietarios;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(length = 60)
    private String fojas;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(columnDefinition = "text")
    private String documentacion;

    @Column(name = "clave_catastral", length = 60)
    private String claveCatastral;

    @Column(name = "fecha_ultimo_avaluo")
    private LocalDate fechaUltimoAvaluo;

    @Column(name = "valor_terreno", precision = 16, scale = 2)
    private BigDecimal valorTerreno;

    @Column(name = "valor_construccion", precision = 16, scale = 2)
    private BigDecimal valorConstruccion;

    @Column(name = "valor_total", precision = 16, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "superficie_construccion", precision = 14, scale = 2)
    private BigDecimal superficieConstruccion;

    @Column(name = "superficie_predio", precision = 14, scale = 2)
    private BigDecimal superficiePredio;

    @Override
    public BigDecimal valorDepreciable() {
        return valorConstruccion;
    }

    @Override
    public LocalDate fechaBaseDepreciacion() {
        return fechaAdquisicion;
    }

    @Override
    public String descripcionValorDepreciable() {
        return "el valor de construcción";
    }

    @Override
    public String descripcionFechaBase() {
        return "la fecha de adquisición";
    }
}
