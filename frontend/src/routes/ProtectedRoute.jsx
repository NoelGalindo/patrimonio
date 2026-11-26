import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Box, CircularProgress } from "@mui/material";
import useAuth from "../auth/useAuth";

/**
 * Protege rutas en el cliente (solo para la navegación: la seguridad real la aplica el backend).
 * Sin sesión -> /login. Con sesión pero sin el rol requerido -> /dashboard.
 */
export default function ProtectedRoute({ roles, children }) {
    const { usuario, cargando } = useAuth();
    const location = useLocation();

    if (cargando) {
        return (
            <Box sx={{ minHeight: "60vh", display: "grid", placeItems: "center" }}>
                <CircularProgress />
            </Box>
        );
    }
    if (!usuario) {
        return <Navigate to="/login" replace state={{ from: location.pathname }} />;
    }
    if (roles && !roles.includes(usuario.rol)) {
        return <Navigate to="/dashboard" replace />;
    }
    return children ?? <Outlet />;
}
