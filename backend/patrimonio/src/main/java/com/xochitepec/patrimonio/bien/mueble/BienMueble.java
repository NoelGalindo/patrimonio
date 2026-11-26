package com.xochitepec.patrimonio.bien.mueble;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.xochitepec.patrimonio.bien.BienBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Relación de bienes muebles. */
@Entity
@Table(name = "bien_mueble")
@Getter
@Setter
@NoArgsConstructor
public class BienMueble extends BienBase {

    @Column(name = "no_control_inventario", nullable = false, unique = true, length = 60)
    private String noControlInventario;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(length = 120)
    private String marca;

    @Column(length = 120)
    private String modelo;

    @Column(length = 120)
    private String serie;

    @Column(columnDefinition = "text")
    private String especificaciones;

    @Column(length = 250)
    private String ubicacion;

    @Column(length = 60)
    private String estado;

    /** Servidor público que tiene el bien bajo resguardo. */
    @Column(name = "servidor_publico", length = 200)
    private String servidorPublico;

    @Column(length = 200)
    private String cargo;

    @Column(name = "fecha_resguardo")
    private LocalDate fechaResguardo;

    // Datos de compra
    @Column(name = "documento_soporte", length = 60)
    private String documentoSoporte;

    @Column(name = "no_factura", length = 60)
    private String noFactura;

    @Column(name = "expedida_por", length = 200)
    private String expedidaPor;

    @Column(name = "fecha_factura")
    private LocalDate fechaFactura;

    @Column(precision = 14, scale = 2)
    private BigDecimal costo;
}
