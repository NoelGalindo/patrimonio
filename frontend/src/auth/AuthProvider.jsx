import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "./authContext";
import { SESION_EXPIRADA, tokenStorage } from "../api/client";
import { authApi } from "../api/endpoints";

export default function AuthProvider({ children }) {
    // Si hay token guardado, se valida contra /auth/me antes de mostrar rutas privadas
    const [estado, setEstado] = useState(() => ({
        usuario: null,
        cargando: Boolean(tokenStorage.get()),
    }));

    useEffect(() => {
        if (!tokenStorage.get()) return;
        let vivo = true;
        authApi.me().then(
            (usuario) => vivo && setEstado({ usuario, cargando: false }),
            () => {
                tokenStorage.clear();
                if (vivo) setEstado({ usuario: null, cargando: false });
            }
        );
        return () => {
            vivo = false;
        };
    }, []);

    useEffect(() => {
        const alExpirar = () => setEstado({ usuario: null, cargando: false });
        window.addEventListener(SESION_EXPIRADA, alExpirar);
        return () => window.removeEventListener(SESION_EXPIRADA, alExpirar);
    }, []);

    const login = useCallback(async (correo, password) => {
        const res = await authApi.login(correo, password);
        tokenStorage.set(res.token);
        setEstado({ usuario: res.usuario, cargando: false });
        return res.usuario;
    }, []);

    const logout = useCallback(() => {
        tokenStorage.clear();
        setEstado({ usuario: null, cargando: false });
    }, []);

    const valor = useMemo(
        () => ({
            usuario: estado.usuario,
            cargando: estado.cargando,
            login,
            logout,
            tieneRol: (...roles) => Boolean(estado.usuario) && roles.includes(estado.usuario.rol),
        }),
        [estado, login, logout]
    );

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
