package com.xochitepec.patrimonio.traspaso;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.xochitepec.patrimonio.bien.mueble.BienMueble;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un bien incluido en la solicitud. Las columnas snap* congelan los datos del bien al autorizar,
 * para poder regenerar el vale idéntico aunque el bien se modifique después.
 */
@Entity
@Table(name = "solicitud_traspaso_detalle")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTraspasoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudTraspaso solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bien_mueble_id", nullable = false)
    private BienMueble bienMueble;

    @Column(name = "snap_no_inventario", length = 60)
    private String snapNoInventario;

    @Column(name = "snap_descripcion", length = 300)
    private String snapDescripcion;

    @Column(name = "snap_marca", length = 120)
    private String snapMarca;

    @Column(name = "snap_modelo", length = 120)
    private String snapModelo;

    @Column(name = "snap_serie", length = 120)
    private String snapSerie;

    @Column(name = "snap_especificaciones", columnDefinition = "text")
    private String snapEspecificaciones;

    @Column(name = "snap_estado", length = 60)
    private String snapEstado;

    @Column(name = "snap_costo", precision = 14, scale = 2)
    private BigDecimal snapCosto;

    @Column(name = "snap_documento_soporte", length = 60)
    private String snapDocumentoSoporte;

    @Column(name = "snap_no_factura", length = 60)
    private String snapNoFactura;

    @Column(name = "snap_expedida_por", length = 200)
    private String snapExpedidaPor;

    @Column(name = "snap_fecha_factura")
    private LocalDate snapFechaFactura;

    @Column(name = "snap_entrega_nombre", length = 200)
    private String snapEntregaNombre;

    @Column(name = "snap_entrega_cargo", length = 200)
    private String snapEntregaCargo;

    public boolean tieneSnapshot() {
        return snapNoInventario != null;
    }

    public void tomarSnapshot(BienMueble b) {
        snapNoInventario = b.getNoControlInventario();
        snapDescripcion = b.getDescripcion();
        snapMarca = b.getMarca();
        snapModelo = b.getModelo();
        snapSerie = b.getSerie();
        snapEspecificaciones = b.getEspecificaciones();
        snapEstado = b.getEstado();
        snapCosto = b.getCosto();
        snapDocumentoSoporte = b.getDocumentoSoporte();
        snapNoFactura = b.getNoFactura();
        snapExpedidaPor = b.getExpedidaPor();
        snapFechaFactura = b.getFechaFactura();
        snapEntregaNombre = b.getServidorPublico();
        snapEntregaCargo = b.getCargo();
    }
}
