import { Routes, Route, Navigate } from "react-router-dom";

import AuthLayout from "../layouts/AuthLayout";
import DashboardLayout from "../layouts/DashboardLayout";

import Login from "../pages/Login";
import Dashboard from "../pages/Dashboard";

export default function AppRoutes() {

    return (

        <Routes>

            {/* Redirección inicial */}
            <Route
                path="/"
                element={<Navigate to="/login" replace />}
            />

            {/* Rutas públicas */}
            <Route element={<AuthLayout />}>
                <Route
                    path="/login"
                    element={<Login />}
                />
            </Route>

            {/* Rutas privadas */}
            <Route element={<DashboardLayout />}>
                <Route
                    path="/dashboard"
                    element={<Dashboard />}
                />
            </Route>

        </Routes>

    );

}