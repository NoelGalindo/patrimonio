import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    Skeleton,
    Typography,
} from "@mui/material";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import PeopleIcon from "@mui/icons-material/PeopleOutlined";
import SettingsIcon from "@mui/icons-material/SettingsOutlined";
import AccountTreeIcon from "@mui/icons-material/AccountTreeOutlined";
import useAuth from "../auth/useAuth";
import { ROLES } from "../auth/roles";
import useFetch from "../hooks/useFetch";
import { etiquetaTraspaso, formatoFechaHora } from "../utils/formato";
import EstatusTraspasoChip from "../components/EstatusTraspasoChip";
import SolicitarTraspasoDialog from "../components/SolicitarTraspasoDialog";
import Aviso from "../components/Aviso";
import TablaResponsiva from "../components/TablaResponsiva";

function Contador({ titulo, valor, descripcion, color, onClick }) {
    return (
        <Card sx={{ minHeight: { xs: 120, sm: 140 }, height: "100%", display: "flex", alignItems: "center", borderTop: "4px solid", borderTopColor: color, cursor: onClick ? "pointer" : "default" }} onClick={onClick}>
            <CardContent sx={{ width: "100%", p: { xs: 1.5, sm: 2 }, "&:last-child": { pb: { xs: 1.5, sm: 2 } } }}>
                <Typography variant="caption" sx={{ fontWeight: "bold", color: "text.secondary", textTransform: "uppercase", letterSpacing: { xs: 0.3, sm: 1 }, lineHeight: 1.3, display: "block" }}>
                    {titulo}
                </Typography>
                <Typography variant="h3" sx={{ fontWeight: "800", mt: { xs: 0.5, sm: 1 }, color }}>
                    {valor ?? <Skeleton width={60} />}
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.5 }}>
                    {descripcion}
                </Typography>
            </CardContent>
        </Card>
    );
}

export default function Dashboard() {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const { rol } = usuario;
    const esUsuario = rol === ROLES.USUARIO;
    const [solicitud, setSolicitud] = useState(null);
    const [aviso, setAviso] = useState(null);

    // Los conteos ya vienen filtrados por área desde el backend cuando el rol es USUARIO
    const muebles = useFetch("/bienes/muebles", { size: 1 });
    const inmuebles = useFetch("/bienes/inmuebles", { size: 1 });
    const vehiculos = useFetch("/bienes/vehiculos", { size: 1 });
    const pendientes = useFetch("/traspasos", { estatus: "PENDIENTE", size: 5 });
    const recientes = useFetch("/traspasos", { size: 6 });

    const error = muebles.error || pendientes.error;

    const seccionRol = () => {
        if (rol === ROLES.PATRIMONIO) {
            const lista = pendientes.data?.contenido ?? [];
            return (
                <Card sx={{ borderLeft: "6px solid", borderLeftColor: "secondary.main" }}>
                    <CardContent sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                        <Box sx={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: 2 }}>
                            <Box>
                                <Typography variant="h6" sx={{ fontWeight: "bold" }}>
                                    Traspasos por autorizar ({pendientes.data?.totalElementos ?? "…"})
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Solicitudes de las áreas que requieren autorización de Patrimonio.
                                </Typography>
                            </Box>
                            <Box sx={{ display: "flex", gap: 1.5, flexWrap: "wrap", "& > *": { flex: { xs: "1 1 100%", sm: "0 0 auto" } } }}>
                                <Button variant="contained" startIcon={<AddCircleIcon />} onClick={() => navigate("/dashboard/altas")}>
                                    Registrar alta
                                </Button>
                                <Button variant="outlined" color="info" endIcon={<ArrowForwardIcon />} onClick={() => navigate("/dashboard/transferencias")}>
                                    Revisar traspasos
                                </Button>
                                <Button variant="outlined" startIcon={<PeopleIcon />} onClick={() => navigate("/dashboard/usuarios")}>
                                    Usuarios de área
                                </Button>
                                <Button variant="outlined" startIcon={<AccountTreeIcon />} onClick={() => navigate("/dashboard/configuracion")}>
                                    Áreas
                                </Button>
                            </Box>
                        </Box>
                        {lista.map((t) => (
                            <Alert key={t.id} severity="warning" variant="outlined" sx={{ borderRadius: 2 }}>
                                <strong>{t.bienes.map((b) => b.noInventario).join(", ")}</strong> — {t.areaOrigen.nombre} → {t.areaDestino.nombre}
                                <Typography variant="caption" sx={{ display: "block" }}>
                                    {t.solicitante.nombreCompleto} · {formatoFechaHora(t.fechaSolicitud)}
                                </Typography>
                            </Alert>
                        ))}
                    </CardContent>
                </Card>
            );
        }
        if (rol === ROLES.ADMINISTRADOR) {
            return (
                <Card sx={{ borderLeft: "6px solid", borderLeftColor: "primary.main" }}>
                    <CardContent sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                        <Box>
                            <Typography variant="h6" sx={{ fontWeight: "bold" }}>Administración del sistema</Typography>
                            <Typography variant="body2" color="text.secondary">
                                Crea usuarios, asigna roles y áreas, y mantiene el catálogo de direcciones y áreas.
                            </Typography>
                        </Box>
                        <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1.5, "& > *": { flex: { xs: "1 1 100%", sm: "0 0 auto" } } }}>
                            <Button variant="contained" startIcon={<PeopleIcon />} onClick={() => navigate("/dashboard/usuarios")}>
                                Gestionar usuarios
                            </Button>
                            <Button variant="outlined" startIcon={<SettingsIcon />} onClick={() => navigate("/dashboard/configuracion")}>
                                Áreas y firmantes
                            </Button>
                        </Box>
                    </CardContent>
                </Card>
            );
        }
        return (
            <Card sx={{ borderLeft: "6px solid", borderLeftColor: "info.main" }}>
                <CardContent sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 2 }}>
                    <Box>
                        <Typography variant="h6" sx={{ fontWeight: "bold" }}>Resguardo de área: {usuario.area?.nombre ?? "sin área"}</Typography>
                        <Typography variant="body2" color="text.secondary">
                            Tienes {pendientes.data?.totalElementos ?? "…"} solicitud(es) de traspaso pendientes de autorización.
                        </Typography>
                    </Box>
                    <Box sx={{ display: "flex", gap: 1.5, flexWrap: "wrap", width: { xs: "100%", sm: "auto" }, "& > *": { flex: { xs: "1 1 100%", sm: "0 0 auto" } } }}>
                        <Button variant="contained" color="info" startIcon={<SwapHorizIcon />} onClick={() => setSolicitud(Date.now())} disabled={!usuario.area}>
                            Solicitar traspaso
                        </Button>
                        <Button variant="outlined" endIcon={<ArrowForwardIcon />} onClick={() => navigate("/dashboard/inventario")}>
                            Ver mi inventario
                        </Button>
                    </Box>
                </CardContent>
            </Card>
        );
    };

    const filas = recientes.data?.contenido ?? [];

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3.5 }}>
            <Box>
                <Typography variant="h4" sx={{ fontWeight: "800", color: "text.primary" }}>
                    ¡Bienvenido, {usuario.nombreCompleto}!
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mt: 0.5 }}>
                    Panel de control del Patrimonio Municipal de Xochitepec.{" "}
                    {esUsuario ? (
                        <>Área: <strong>{usuario.area?.nombre ?? "sin área asignada"}</strong>.</>
                    ) : (
                        <>Vista de <strong>todas las áreas</strong>.</>
                    )}
                </Typography>
            </Box>

            {esUsuario && !usuario.area && (
                <Alert severity="warning">Tu usuario no tiene un área asignada. Solicita al administrador que te asigne una.</Alert>
            )}
            {error && <Alert severity="error">{error.message}</Alert>}

            <Grid container spacing={3}>
                <Grid size={{ xs: 6, md: 3 }}>
                    <Contador titulo="Bienes muebles" valor={muebles.data?.totalElementos} descripcion="Activos en inventario" color="info.main" onClick={() => navigate("/dashboard/inventario?tipo=muebles")} />
                </Grid>
                <Grid size={{ xs: 6, md: 3 }}>
                    <Contador titulo="Bienes inmuebles" valor={inmuebles.data?.totalElementos} descripcion="Predios y edificaciones" color="primary.main" onClick={() => navigate("/dashboard/inventario?tipo=inmuebles")} />
                </Grid>
                <Grid size={{ xs: 6, md: 3 }}>
                    <Contador titulo="Vehículos" valor={vehiculos.data?.totalElementos} descripcion="Parque vehicular" color="secondary.main" onClick={() => navigate("/dashboard/inventario?tipo=vehiculos")} />
                </Grid>
                <Grid size={{ xs: 6, md: 3 }}>
                    <Contador titulo="Traspasos pendientes" valor={pendientes.data?.totalElementos} descripcion="En espera de autorización" color="warning.main" onClick={() => navigate("/dashboard/transferencias")} />
                </Grid>
            </Grid>

            {seccionRol()}

            <Card>
                <CardContent>
                    <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 1, mb: 2.5 }}>
                        <Typography variant="h6" sx={{ fontWeight: "bold" }}>Últimos traspasos</Typography>
                        <Button size="small" variant="text" endIcon={<ArrowForwardIcon />} onClick={() => navigate("/dashboard/transferencias")}>
                            Ver todos
                        </Button>
                    </Box>
                    <TablaResponsiva
                        columnas={[
                            { id: "folio", label: "Folio", render: (t) => etiquetaTraspaso(t), principal: true },
                            {
                                id: "bienes",
                                label: "Bienes",
                                secundaria: true,
                                render: (t) => t.bienes.map((b) => `${b.noInventario} ${b.descripcion}`).join(", "),
                            },
                            { id: "ruta", label: "Origen → Destino", render: (t) => `${t.areaOrigen.nombre} → ${t.areaDestino.nombre}` },
                            { id: "fecha", label: "Fecha", render: (t) => formatoFechaHora(t.fechaSolicitud) },
                            { id: "estatus", label: "Estatus", render: (t) => <EstatusTraspasoChip estatus={t.estatus} /> },
                        ]}
                        filas={filas}
                        cargando={recientes.cargando}
                        onClickFila={() => navigate("/dashboard/transferencias")}
                        textoVacio="Aún no hay movimientos."
                    />
                </CardContent>
            </Card>

            {solicitud && (
                <SolicitarTraspasoDialog
                    key={solicitud}
                    open
                    onClose={() => setSolicitud(null)}
                    onCreada={() => {
                        pendientes.recargar();
                        recientes.recargar();
                        setAviso({ tipo: "success", mensaje: "Solicitud enviada a Patrimonio para su autorización." });
                    }}
                />
            )}
            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}
