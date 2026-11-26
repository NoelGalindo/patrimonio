import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { Alert, Box, Button, Paper, TextField, Typography } from "@mui/material";
import useAuth from "../auth/useAuth";

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const { usuario, login } = useAuth();
    const [correo, setCorreo] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [enviando, setEnviando] = useState(false);

    if (usuario) {
        return <Navigate to="/dashboard" replace />;
    }

    const handleLogin = async (e) => {
        e.preventDefault();
        if (correo.trim() === "" || password === "") {
            setError("Por favor ingresa tu correo y contraseña.");
            return;
        }
        setEnviando(true);
        setError("");
        try {
            await login(correo.trim(), password);
            navigate(location.state?.from ?? "/dashboard", { replace: true });
        } catch (err) {
            setError(err.message);
            setEnviando(false);
        }
    };

    return (
        <Paper
            elevation={5}
            sx={{
                width: 440,
                maxWidth: "100%",
                p: { xs: 3, sm: 4.5 },
                borderRadius: 4,
                display: "flex",
                flexDirection: "column",
                gap: 2.5,
            }}
        >
            <Box sx={{ textAlign: "center", mb: 1 }}>
                <Typography variant="h4" fontWeight="800" color="primary.main" gutterBottom>
                    Bienvenido
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Inicia sesión para acceder al control de patrimonio.
                </Typography>
            </Box>

            {error && <Alert severity="error" sx={{ borderRadius: 2 }}>{error}</Alert>}

            <Box component="form" onSubmit={handleLogin} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                <TextField
                    fullWidth
                    label="Correo electrónico"
                    type="email"
                    autoComplete="username"
                    value={correo}
                    onChange={(e) => setCorreo(e.target.value)}
                    placeholder="usuario@xochitepec.gob.mx"
                    autoFocus
                />

                <TextField
                    fullWidth
                    label="Contraseña"
                    type="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                />

                <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    size="large"
                    disabled={enviando}
                    sx={{
                        mt: 1.5,
                        py: 1.3,
                        fontSize: "0.95rem",
                        backgroundColor: "primary.main",
                        "&:hover": {
                            backgroundColor: "primary.dark",
                        },
                    }}
                >
                    {enviando ? "Verificando…" : "Iniciar Sesión"}
                </Button>
            </Box>
        </Paper>
    );
}
