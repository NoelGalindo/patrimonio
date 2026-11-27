package com.xochitepec.patrimonio.bien;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.xochitepec.patrimonio.catalogo.Area;
import com.xochitepec.patrimonio.depreciacion.CuentaContable;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

/** Columnas comunes a todos los tipos de bien: área a la que pertenece, baja lógica y auditoría. */
@MappedSuperclass
@Getter
@Setter
public abstract class BienBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    /** Cuenta de la Guía de Vida Útil del CONAC: define vida útil y % de depreciación. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_contable_id")
    private CuentaContable cuentaContable;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_por", updatable = false)
    private Long creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    /** Valor sobre el que se calcula la depreciación (costo, valor de factura o valor de construcción). */
    public abstract BigDecimal valorDepreciable();

    /** Fecha desde la que se cuenta la depreciación (fecha de factura o de adquisición). */
    public abstract LocalDate fechaBaseDepreciacion();

    /** Nombre del valor para mensajes ("el costo", "el valor de construcción"...). */
    public abstract String descripcionValorDepreciable();

    /** Nombre de la fecha para mensajes ("la fecha de factura"...). */
    public abstract String descripcionFechaBase();

    @PrePersist
    void alCrear() {
        creadoEn = Instant.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = Instant.now();
    }
}
