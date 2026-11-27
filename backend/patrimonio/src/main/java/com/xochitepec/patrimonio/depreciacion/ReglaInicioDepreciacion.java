package com.xochitepec.patrimonio.depreciacion;

/**
 * Desde cuándo empieza a depreciarse un bien. La elige el ADMINISTRADOR
 * (parámetro {@code depreciacion.inicio}) y aplica a todos los bienes.
 */
public enum ReglaInicioDepreciacion {
    /** Años completos a partir del 1 de enero del año siguiente a la compra. */
    ANIO_SIGUIENTE,
    /** El año de la compra cuenta como primer año completo. */
    ANIO_ADQUISICION,
    /** Años completos contados desde la fecha de compra (cada aniversario). */
    ANIVERSARIO,
    /** Proporcional por mes, desde el mes siguiente a la compra. */
    MES_SIGUIENTE;

    public static final String CLAVE_PARAMETRO = "depreciacion.inicio";
    public static final ReglaInicioDepreciacion PREDETERMINADA = ANIO_SIGUIENTE;
}
