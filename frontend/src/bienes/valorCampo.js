import { formatoFecha, formatoMoneda, formatoNumero } from "../utils/formato";

/** Texto para mostrar el valor de un campo de bien según su tipo. */
export default function valorCampo(campo, bien) {
    const v = bien?.[campo.name];
    if (v === null || v === undefined || v === "") return "";
    switch (campo.type) {
        case "date":
            return formatoFecha(v);
        case "money":
            return formatoMoneda(v);
        case "number":
            return formatoNumero(v);
        default:
            return String(v);
    }
}
