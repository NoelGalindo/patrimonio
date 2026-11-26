import { useEffect, useState } from "react";

/** Devuelve `valor` después de que deja de cambiar durante `ms` milisegundos. */
export default function useDebounce(valor, ms = 350) {
    const [retrasado, setRetrasado] = useState(valor);
    useEffect(() => {
        const t = setTimeout(() => setRetrasado(valor), ms);
        return () => clearTimeout(t);
    }, [valor, ms]);
    return retrasado;
}
