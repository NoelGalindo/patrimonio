// Cliente HTTP para la API de Spring Boot.
// En desarrollo, Vite redirige /api a http://localhost:8080 (ver vite.config.js).
// En producción puede definirse VITE_API_URL con la URL base del backend.

const BASE_URL = import.meta.env.VITE_API_URL ?? "";
const TOKEN_KEY = "patrimonio.token";

export const tokenStorage = {
    get() {
        try {
            return localStorage.getItem(TOKEN_KEY);
        } catch {
            return null;
        }
    },
    set(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    clear() {
        localStorage.removeItem(TOKEN_KEY);
    },
};

// Evento global para que AuthProvider cierre la sesión cuando el token expira o se revoca
export const SESION_EXPIRADA = "patrimonio:sesion-expirada";

export class ApiError extends Error {
    constructor(status, mensaje, campos) {
        super(mensaje);
        this.status = status;
        this.campos = campos ?? null;
    }
}

function construirUrl(path, params) {
    const qs = new URLSearchParams();
    Object.entries(params ?? {}).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== "") qs.append(k, v);
    });
    const query = qs.toString();
    return `${BASE_URL}/api${path}${query ? `?${query}` : ""}`;
}

/**
 * Llama a la API. Lanza ApiError con el mensaje en español que devuelve el backend.
 * @param {string} path  ruta sin el prefijo /api, p. ej. "/bienes/muebles"
 * @param {{method?: string, body?: any, params?: object, blob?: boolean}} opciones
 */
export async function api(path, { method = "GET", body, params, blob = false } = {}) {
    const token = tokenStorage.get();
    const headers = {};
    if (token) headers.Authorization = `Bearer ${token}`;
    if (body !== undefined) headers["Content-Type"] = "application/json";

    let res;
    try {
        res = await fetch(construirUrl(path, params), {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        throw new ApiError(0, "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.");
    }

    if (res.status === 401 && token) {
        tokenStorage.clear();
        window.dispatchEvent(new Event(SESION_EXPIRADA));
    }

    if (!res.ok) {
        let data = null;
        try {
            data = await res.json();
        } catch {
            // respuesta sin cuerpo JSON
        }
        throw new ApiError(res.status, data?.mensaje ?? `Error ${res.status}`, data?.campos);
    }

    if (blob) return res.blob();
    if (res.status === 204) return null;
    return res.json();
}

/** Descarga un archivo protegido (p. ej. el vale PDF) usando el token de la sesión. */
export async function descargarArchivo(path, nombreArchivo) {
    const archivo = await api(path, { blob: true });
    const url = URL.createObjectURL(archivo);
    const a = document.createElement("a");
    a.href = url;
    a.download = nombreArchivo;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
}
