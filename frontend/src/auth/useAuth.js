import { useContext } from "react";
import { AuthContext } from "./authContext";

/**
 * Sesión actual: { usuario, cargando, login, logout, tieneRol }.
 * usuario = { id, nombreCompleto, correo, rol, area: { id, nombre, direccionGeneral } | null }
 */
export default function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth debe usarse dentro de <AuthProvider>");
    return ctx;
}
