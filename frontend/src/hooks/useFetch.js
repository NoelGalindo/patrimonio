import { useCallback, useEffect, useState } from "react";
import { api } from "../api/client";

/**
 * Obtiene datos de la API y los vuelve a pedir cuando cambian `path` o `params`.
 * Pasa `path = null` para no hacer la petición.
 * Devuelve { data, error, cargando, recargar }.
 */
export default function useFetch(path, params) {
    const clave = path ? JSON.stringify([path, params ?? {}]) : null;
    const [version, setVersion] = useState(0);
    const [estado, setEstado] = useState({ clave: null, data: null, error: null });
    const claveActual = clave ? `${clave}#${version}` : null;

    useEffect(() => {
        if (!claveActual) return;
        const [p, prm] = JSON.parse(claveActual.slice(0, claveActual.lastIndexOf("#")));
        let vivo = true;
        api(p, { params: prm }).then(
            (data) => vivo && setEstado({ clave: claveActual, data, error: null }),
            (error) => vivo && setEstado({ clave: claveActual, data: null, error })
        );
        return () => {
            vivo = false;
        };
    }, [claveActual]);

    const recargar = useCallback(() => setVersion((v) => v + 1), []);
    const listo = estado.clave === claveActual;

    return {
        // Mientras recarga se conservan los datos anteriores para evitar parpadeos
        data: estado.data,
        error: listo ? estado.error : null,
        cargando: Boolean(claveActual) && !listo,
        recargar,
    };
}
