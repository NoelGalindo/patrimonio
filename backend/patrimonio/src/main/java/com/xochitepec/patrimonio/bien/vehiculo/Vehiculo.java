package com.xochitepec.patrimonio.bien.vehiculo;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.xochitepec.patrimonio.bien.BienBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Relación general de vehículos. El "área designada" es el área heredada de {@link BienBase}. */
@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo extends BienBase {

    @Column(length = 30)
    private String numero;

    @Column(name = "no_economico", length = 60)
    private String noEconomico;

    @Column(name = "no_inventario", nullable = false, unique = true, length = 60)
    private String noInventario;

    @Column(name = "folio_resguardo", length = 60)
    private String folioResguardo;

    @Column(name = "fecha_revision")
    private LocalDate fechaRevision;

    @Column(nullable = false, length = 120)
    private String marca;

    @Column(length = 120)
    private String version;

    @Column(length = 60)
    private String color;

    @Column(length = 60)
    private String modelo;

    @Column(name = "no_serie", unique = true, length = 60)
    private String noSerie;

    @Column(length = 20)
    private String cilindros;

    @Column(name = "no_motor", length = 60)
    private String noMotor;

    @Column(length = 30)
    private String placas;

    @Column(columnDefinition = "text")
    private String documentacion;

    @Column(length = 200)
    private String emisor;

    @Column(name = "fecha_factura")
    private LocalDate fechaFactura;

    @Column(name = "folio_factura", length = 60)
    private String folioFactura;

    @Column(name = "valor_factura", precision = 14, scale = 2)
    private BigDecimal valorFactura;

    @Column(name = "valor_depreciado", precision = 14, scale = 2)
    private BigDecimal valorDepreciado;

    @Column(length = 150)
    private String propiedad;

    @Column(length = 250)
    private String condiciones;
}
