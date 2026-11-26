// Definición de los tres tipos de bien según "Estructura patrimonio.xlsx".
// La usan el formulario de altas, la tabla de inventario y el diálogo de detalle.
//
// tipo de campo: text | textarea | date | number | money
// columna: true si aparece en la tabla del inventario

export const TIPOS_BIEN = {
    muebles: {
        label: "Bienes muebles",
        singular: "bien mueble",
        clave: "noControlInventario",
        titulo: "descripcion",
        secciones: [
            {
                titulo: "Identificación del bien",
                campos: [
                    { name: "noControlInventario", label: "No. de control de inventario", required: true, columna: true, size: 4 },
                    { name: "descripcion", label: "Descripción del artículo", required: true, columna: true, size: 8 },
                    { name: "marca", label: "Marca", columna: true, size: 4 },
                    { name: "modelo", label: "Modelo", columna: true, size: 4 },
                    { name: "serie", label: "Serie", columna: true, size: 4 },
                    { name: "especificaciones", label: "Especificaciones del bien", type: "textarea", size: 12 },
                    { name: "estado", label: "Estado", columna: true, size: 4, opciones: ["BUENO", "REGULAR", "MALO"] },
                    { name: "ubicacion", label: "Ubicación", size: 8 },
                ],
            },
            {
                titulo: "Resguardo",
                campos: [
                    { name: "servidorPublico", label: "Nombre del servidor público", columna: true, size: 6 },
                    { name: "cargo", label: "Cargo", size: 6 },
                    { name: "fechaResguardo", label: "Fecha de resguardo", type: "date", size: 4 },
                ],
            },
            {
                titulo: "Datos de compra",
                campos: [
                    { name: "documentoSoporte", label: "Documento soporte para alta", size: 4, placeholder: "FACTURA" },
                    { name: "noFactura", label: "No. de factura", size: 4 },
                    { name: "fechaFactura", label: "Fecha de factura", type: "date", size: 4 },
                    { name: "expedidaPor", label: "Expedida por", size: 8 },
                    { name: "costo", label: "Costo del bien", type: "money", columna: true, size: 4 },
                ],
            },
        ],
    },
    inmuebles: {
        label: "Bienes inmuebles",
        singular: "bien inmueble",
        clave: "noExpediente",
        titulo: "denominacion",
        secciones: [
            {
                titulo: "Identificación del predio",
                campos: [
                    { name: "noExpediente", label: "No. de expediente", required: true, columna: true, size: 4 },
                    { name: "denominacion", label: "Denominación", required: true, columna: true, size: 8 },
                    { name: "colonia", label: "Colonia", columna: true, size: 4 },
                    { name: "calle", label: "Calle", size: 4 },
                    { name: "tipoUso", label: "Tipo de uso", columna: true, size: 4 },
                    { name: "medidasColindancias", label: "Medidas y colindancias", type: "textarea", size: 12 },
                    { name: "latitud", label: "Latitud (Google Maps)", type: "number", size: 3 },
                    { name: "longitud", label: "Longitud (Google Maps)", type: "number", size: 3 },
                    { name: "propietarios", label: "Propietarios", size: 6 },
                    { name: "fechaRevision", label: "Fecha de revisión", type: "date", size: 4 },
                    { name: "fechaAdquisicion", label: "Fecha de adquisición", type: "date", size: 4 },
                    { name: "fojas", label: "Fojas", size: 4 },
                ],
            },
            {
                titulo: "Datos catastrales y valores",
                campos: [
                    { name: "claveCatastral", label: "Clave catastral", columna: true, size: 4 },
                    { name: "fechaUltimoAvaluo", label: "Fecha del último avalúo catastral", type: "date", size: 4 },
                    { name: "superficiePredio", label: "Superficie del predio (m²)", type: "number", size: 4 },
                    { name: "superficieConstruccion", label: "Superficie de construcción (m²)", type: "number", size: 4 },
                    { name: "valorTerreno", label: "Valor del terreno", type: "money", size: 4 },
                    { name: "valorConstruccion", label: "Valor de la construcción", type: "money", size: 4 },
                    { name: "valorTotal", label: "Total del valor", type: "money", columna: true, size: 4, ayuda: "Si se deja vacío se calcula como terreno + construcción" },
                ],
            },
            {
                titulo: "Documentación",
                campos: [
                    { name: "documentacion", label: "Documentación", type: "textarea", size: 6 },
                    { name: "observaciones", label: "Observaciones", type: "textarea", size: 6 },
                ],
            },
        ],
    },
    vehiculos: {
        label: "Vehículos",
        singular: "vehículo",
        clave: "noInventario",
        titulo: "marca",
        secciones: [
            {
                titulo: "Identificación del vehículo",
                campos: [
                    { name: "noInventario", label: "No. de inventario", required: true, columna: true, size: 4 },
                    { name: "numero", label: "No.", size: 2 },
                    { name: "noEconomico", label: "No. económico", columna: true, size: 3 },
                    { name: "folioResguardo", label: "No. folio de resguardo", size: 3 },
                    { name: "marca", label: "Marca", required: true, columna: true, size: 4 },
                    { name: "version", label: "Versión", columna: true, size: 4 },
                    { name: "modelo", label: "Modelo (año)", columna: true, size: 4 },
                    { name: "color", label: "Color", size: 3 },
                    { name: "cilindros", label: "Cilindros", size: 3 },
                    { name: "placas", label: "Número de placas", columna: true, size: 3 },
                    { name: "fechaRevision", label: "Fecha de revisión", type: "date", size: 3 },
                    { name: "noSerie", label: "No. de serie", size: 6 },
                    { name: "noMotor", label: "No. de motor", size: 6 },
                    { name: "condiciones", label: "Condiciones del vehículo", size: 6 },
                    { name: "propiedad", label: "Propiedad", size: 6 },
                ],
            },
            {
                titulo: "Factura y valor",
                campos: [
                    { name: "emisor", label: "Emisor", size: 6 },
                    { name: "folioFactura", label: "Folio de factura", size: 3 },
                    { name: "fechaFactura", label: "Fecha de factura", type: "date", size: 3 },
                    { name: "valorFactura", label: "Valor factura", type: "money", size: 4 },
                    { name: "valorDepreciado", label: "Valor depreciado", type: "money", size: 4 },
                    { name: "documentacion", label: "Documentación", type: "textarea", size: 12 },
                ],
            },
        ],
    },
};

export const CLAVES_TIPO = Object.keys(TIPOS_BIEN);

export function camposDe(tipo) {
    return TIPOS_BIEN[tipo].secciones.flatMap((s) => s.campos);
}

export function columnasDe(tipo) {
    return camposDe(tipo).filter((c) => c.columna);
}

/** Formulario vacío para un tipo de bien. */
export function formularioVacio(tipo) {
    const f = { areaId: "" };
    camposDe(tipo).forEach((c) => {
        f[c.name] = "";
    });
    return f;
}

/** Convierte un bien de la API a valores de formulario (null -> ""). */
export function aFormulario(tipo, bien) {
    const f = formularioVacio(tipo);
    Object.keys(f).forEach((k) => {
        if (bien[k] !== null && bien[k] !== undefined) f[k] = bien[k];
    });
    f.areaId = bien.area?.id ?? "";
    return f;
}

/** Convierte valores de formulario al cuerpo que espera la API ("" -> null, números). */
export function aPeticion(tipo, form) {
    const body = { areaId: form.areaId === "" ? null : Number(form.areaId) };
    camposDe(tipo).forEach((c) => {
        const v = form[c.name];
        if (v === "" || v === undefined) body[c.name] = null;
        else if (c.type === "number" || c.type === "money") body[c.name] = Number(v);
        else body[c.name] = typeof v === "string" ? v.trim() : v;
    });
    return body;
}
