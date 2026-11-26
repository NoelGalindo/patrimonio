import { Routes, Route, Navigate } from "react-router-dom";

import AuthLayout from "../layouts/AuthLayout";
import DashboardLayout from "../layouts/DashboardLayout";
import ProtectedRoute from "./ProtectedRoute";
import { ROLES } from "../auth/roles";

import Login from "../pages/Login";
import Dashboard from "../pages/Dashboard";
import Inventario from "../pages/Inventario";
import Altas from "../pages/Altas";
import Bajas from "../pages/Bajas";
import Transferencias from "../pages/Transferencias";
import Usuarios from "../pages/Usuarios";
import Configuracion from "../pages/Configuracion";

const { ADMINISTRADOR, PATRIMONIO } = ROLES;

export default function AppRoutes() {

    return (

        <Routes>

            {/* Redirección inicial */}
            <Route
                path="/"
                element={<Navigate to="/dashboard" replace />}
            />

            {/* Rutas públicas */}
            <Route element={<AuthLayout />}>
                <Route
                    path="/login"
                    element={<Login />}
                />
            </Route>

            {/* Rutas privadas: requieren sesión */}
            <Route
                element={
                    <ProtectedRoute>
                        <DashboardLayout />
                    </ProtectedRoute>
                }
            >
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/dashboard/inventario" element={<Inventario />} />
                <Route path="/dashboard/transferencias" element={<Transferencias />} />

                {/* Solo Patrimonio captura bienes */}
                <Route element={<ProtectedRoute roles={[PATRIMONIO]} />}>
                    <Route path="/dashboard/altas" element={<Altas />} />
                </Route>

                <Route element={<ProtectedRoute roles={[PATRIMONIO, ADMINISTRADOR]} />}>
                    <Route path="/dashboard/bajas" element={<Bajas />} />
                </Route>

                {/* Administrador y Patrimonio gestionan usuarios y áreas
                    (Patrimonio solo usuarios de área; el backend aplica el alcance) */}
                <Route element={<ProtectedRoute roles={[ADMINISTRADOR, PATRIMONIO]} />}>
                    <Route path="/dashboard/usuarios" element={<Usuarios />} />
                    <Route path="/dashboard/configuracion" element={<Configuracion />} />
                </Route>
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />

        </Routes>

    );

}
