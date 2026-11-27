// Depreciación según la Guía de Vida Útil del CONAC. Valores iguales a los enums del backend.

export const CLAVE_REGLA = "depreciacion.inicio";

export const REGLAS_DEPRECIACION = [
    {
        valor: "ANIO_SIGUIENTE",
        label: "Año completo, desde el año siguiente a la compra",
        ejemplo: "Comprado el 15/08/2026: deprecia años completos a partir del 01/01/2027.",
    },
    {
        valor: "ANIO_ADQUISICION",
        label: "Año completo, contando el año de la compra",
        ejemplo: "Comprado el 15/08/2026: 2026 ya cuenta como primer año completo.",
    },
    {
        valor: "ANIVERSARIO",
        label: "Años completos desde la fecha de compra",
        ejemplo: "Comprado el 15/08/2026: cumple su primer año de depreciación el 15/08/2027.",
    },
    {
        valor: "MES_SIGUIENTE",
        label: "Proporcional por mes, desde el mes siguiente a la compra",
        ejemplo: "Comprado el 15/08/2026: deprecia mes a mes a partir del 01/09/2026.",
    },
];

export const etiquetaRegla = (valor) => REGLAS_DEPRECIACION.find((r) => r.valor === valor)?.label ?? valor;

export const TIPOS_CONTABLES = [
    { valor: "MUEBLE", label: "Bien mueble" },
    { valor: "VEHICULO", label: "Vehículo / equipo de transporte" },
    { valor: "INMUEBLE", label: "Bien inmueble" },
];

export const etiquetaTipoContable = (valor) => TIPOS_CONTABLES.find((t) => t.valor === valor)?.label ?? valor;
