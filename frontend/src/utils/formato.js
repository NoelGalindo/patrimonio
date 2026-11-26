const moneda = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });
const numero = new Intl.NumberFormat("es-MX", { maximumFractionDigits: 2 });

/** "2026-07-01" o ISO con hora -> "01/07/2026" */
export function formatoFecha(valor) {
    if (!valor) return "";
    if (/^\d{4}-\d{2}-\d{2}$/.test(valor)) {
        const [a, m, d] = valor.split("-");
        return `${d}/${m}/${a}`;
    }
    return new Date(valor).toLocaleDateString("es-MX", { day: "2-digit", month: "2-digit", year: "numeric" });
}

export function formatoFechaHora(valor) {
    if (!valor) return "";
    return new Date(valor).toLocaleString("es-MX", { dateStyle: "short", timeStyle: "short" });
}

export function formatoMoneda(valor) {
    return valor === null || valor === undefined || valor === "" ? "" : moneda.format(Number(valor));
}

export function formatoNumero(valor) {
    return valor === null || valor === undefined || valor === "" ? "" : numero.format(Number(valor));
}

export function formatoFolio(t) {
    return t?.folio ? `${String(t.folio).padStart(3, "0")}/${t.anioFolio}` : "—";
}

/** Identificador para mostrar: el folio del vale si ya se autorizó, si no el número de solicitud. */
export function etiquetaTraspaso(t) {
    return t?.folio ? `Folio ${formatoFolio(t)}` : `Solicitud #${t.id}`;
}
