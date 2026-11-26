package com.xochitepec.patrimonio.traspaso;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.usuario.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitud_traspaso")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTraspaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Consecutivo del vale; se asigna únicamente al autorizar. */
    private Integer folio;

    @Column(name = "anio_folio")
    private Integer anioFolio;

    @Column(name = "oficio_peticion", length = 100)
    private String oficioPeticion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_origen_id", nullable = false)
    private Area areaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_destino_id", nullable = false)
    private Area areaDestino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(columnDefinition = "text")
    private String motivo;

    /** Servidor público del área destino que recibe el bien (nuevo resguardante). */
    @Column(name = "recibe_nombre", nullable = false, length = 200)
    private String recibeNombre;

    @Column(name = "recibe_cargo", length = 200)
    private String recibeCargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusTraspaso estatus = EstatusTraspaso.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private Instant fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por_id")
    private Usuario resueltoPor;

    @Column(name = "fecha_resolucion")
    private Instant fechaResolucion;

    @Column(name = "comentario_resolucion", columnDefinition = "text")
    private String comentarioResolucion;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SolicitudTraspasoDetalle> detalles = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fechaSolicitud == null) {
            fechaSolicitud = Instant.now();
        }
    }

    public void agregarDetalle(SolicitudTraspasoDetalle d) {
        d.setSolicitud(this);
        detalles.add(d);
    }
}
