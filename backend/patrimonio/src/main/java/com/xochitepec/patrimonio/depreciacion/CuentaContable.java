package com.xochitepec.patrimonio.depreciacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Cuenta de la Guía de Vida Útil del CONAC (p. ej. 1.2.4.1.3 Equipo de Cómputo, 3 años). */
@Entity
@Table(name = "cuenta_contable")
@Getter
@Setter
public class CuentaContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String clave;

    @Column(nullable = false, length = 200)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bien", nullable = false, length = 20)
    private TipoBienContable tipoBien;

    /** NULL = sin vida útil definida: no se puede asignar a bienes. */
    @Column(name = "vida_util_anios")
    private Integer vidaUtilAnios;

    @Column(nullable = false)
    private boolean activo = true;
}
