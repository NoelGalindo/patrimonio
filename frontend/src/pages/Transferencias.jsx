import { useState } from "react";
import {
    Alert,
    Box,
    Button,
    Card,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    Tab,
    Tabs,
    Typography,
} from "@mui/material";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdfOutlined";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import useAuth from "../auth/useAuth";
import { ROLES } from "../auth/roles";
import useFetch from "../hooks/useFetch";
import { traspasosApi } from "../api/endpoints";
import { etiquetaTraspaso, formatoFechaHora, formatoFolio, formatoMoneda } from "../utils/formato";
import Aviso from "../components/Aviso";
import ConfirmDialog from "../components/ConfirmDialog";
import EstatusTraspasoChip from "../components/EstatusTraspasoChip";
import SolicitarTraspasoDialog from "../components/SolicitarTraspasoDialog";
import TablaResponsiva from "../components/TablaResponsiva";
import EncabezadoPagina from "../components/EncabezadoPagina";
import useEsMovil from "../hooks/useEsMovil";

const FILTROS = [
    { valor: "PENDIENTE", label: "Pendientes" },
    { valor: "AUTORIZADA", label: "Autorizadas" },
    { valor: "", label: "Todas" },
];

export default function Transferencias() {
    const { usuario } = useAuth();
    const esMovil = useEsMovil();
    const esPatrimonio = usuario.rol === ROLES.PATRIMONIO;
    const puedeSolicitar = usuario.rol === ROLES.USUARIO || esPatrimonio;

    const [estatus, setEstatus] = useState("PENDIENTE");
    const [pagina, setPagina] = useState(0);
    const [detalle, setDetalle] = useState(null);
    const [accion, setAccion] = useState(null); // { tipo: "autorizar" | "rechazar" | "cancelar", traspaso }
    const [nueva, setNueva] = useState(null);
    const [aviso, setAviso] = useState(null);

    const { data, error, cargando, recargar } = useFetch("/traspasos", { estatus, page: pagina, size: 20 });
    const filas = data?.contenido ?? [];

    const descargar = async (t) => {
        try {
            await traspasosApi.descargarVale(t);
        } catch (e) {
            setAviso({ tipo: "error", mensaje: e.message });
        }
    };

    const ejecutar = async (comentario) => {
        const { tipo, traspaso } = accion;
        let res;
        if (tipo === "autorizar") res = await traspasosApi.autorizar(traspaso.id, comentario);
        else if (tipo === "rechazar") res = await traspasosApi.rechazar(traspaso.id, comentario);
        else res = await traspasosApi.cancelar(traspaso.id);
        setDetalle(null);
        recargar();
        setAviso({
            tipo: "success",
            mensaje:
                tipo === "autorizar"
                    ? `Traspaso autorizado con folio ${formatoFolio(res)}. Ya puedes descargar el vale.`
                    : tipo === "rechazar"
                    ? "Solicitud rechazada."
                    : "Solicitud cancelada.",
        });
    };

    const botones = (t) => {
        const pendiente = t.estatus === "PENDIENTE";
        return (
            <>
                {t.estatus === "AUTORIZADA" && (
                    <Button size="small" startIcon={<PictureAsPdfIcon />} onClick={() => descargar(t)}>
                        Vale
                    </Button>
                )}
                {pendiente && esPatrimonio && (
                    <>
                        <Button size="small" color="success" startIcon={<CheckIcon />} onClick={() => setAccion({ tipo: "autorizar", traspaso: t })}>
                            Autorizar
                        </Button>
                        <Button size="small" color="error" startIcon={<CloseIcon />} onClick={() => setAccion({ tipo: "rechazar", traspaso: t })}>
                            Rechazar
                        </Button>
                    </>
                )}
                {pendiente && t.solicitante.id === usuario.id && (
                    <Button size="small" color="inherit" onClick={() => setAccion({ tipo: "cancelar", traspaso: t })}>
                        Cancelar
                    </Button>
                )}
            </>
        );
    };

    const textosAccion = {
        autorizar: {
            titulo: "Autorizar traspaso",
            mensaje: "Los bienes pasarán al área destino con el nuevo resguardante y se asignará folio al vale.",
            confirmar: "Autorizar",
            color: "success",
            comentario: "opcional",
        },
        rechazar: {
            titulo: "Rechazar traspaso",
            mensaje: "Indica el motivo del rechazo; el área solicitante podrá verlo.",
            confirmar: "Rechazar",
            color: "error",
            comentario: "obligatorio",
        },
        cancelar: {
            titulo: "Cancelar solicitud",
            mensaje: "¿Deseas cancelar esta solicitud de traspaso?",
            confirmar: "Cancelar solicitud",
            color: "warning",
            comentario: null,
        },
    };
    const ta = accion ? textosAccion[accion.tipo] : null;

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo="Traspasos de Bienes"
                descripcion={
                    esPatrimonio
                        ? "Revisa y autoriza las solicitudes de traspaso de bienes muebles entre áreas."
                        : usuario.rol === ROLES.USUARIO
                        ? "Solicita el traspaso de bienes de tu área y da seguimiento a su autorización."
                        : "Consulta las solicitudes de traspaso entre áreas."
                }
            >
                {puedeSolicitar && (
                    <Button variant="contained" color="info" startIcon={<SwapHorizIcon />} onClick={() => setNueva(Date.now())}>
                        Solicitar traspaso
                    </Button>
                )}
            </EncabezadoPagina>

            <Card>
                <Tabs
                    value={estatus}
                    onChange={(_, v) => {
                        setEstatus(v);
                        setPagina(0);
                    }}
                    sx={{ px: { xs: 0, sm: 2 } }}
                >
                    {FILTROS.map((f) => (
                        <Tab key={f.label} value={f.valor} label={f.label} sx={{ fontWeight: 700 }} />
                    ))}
                </Tabs>
            </Card>

            {error && <Alert severity="error">{error.message}</Alert>}

            <TablaResponsiva
                columnas={[
                    { id: "folio", label: "Folio", render: (t) => etiquetaTraspaso(t), principal: true },
                    {
                        id: "bienes",
                        label: "Bienes",
                        secundaria: true,
                        render: (t) => (
                            <Box sx={{ maxWidth: { sm: 260 } }}>
                                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                    {t.bienes.map((b) => b.noInventario).join(", ")}
                                </Typography>
                                <Typography variant="caption" color="text.secondary" noWrap sx={{ display: "block" }}>
                                    {t.bienes.map((b) => b.descripcion).join(", ")}
                                </Typography>
                            </Box>
                        ),
                    },
                    {
                        id: "ruta",
                        label: "Origen → Destino",
                        render: (t) => (
                            <Box sx={{ fontSize: "0.82rem" }}>
                                <Box component="span" sx={{ color: "text.secondary" }}>{t.areaOrigen.nombre}</Box>
                                <br />
                                <strong>→ {t.areaDestino.nombre}</strong>
                            </Box>
                        ),
                    },
                    { id: "solicito", label: "Solicitó", render: (t) => t.solicitante.nombreCompleto },
                    { id: "fecha", label: "Fecha", render: (t) => formatoFechaHora(t.fechaSolicitud) },
                    { id: "estatus", label: "Estatus", render: (t) => <EstatusTraspasoChip estatus={t.estatus} /> },
                ]}
                filas={filas}
                cargando={cargando}
                onClickFila={setDetalle}
                acciones={botones}
                minWidth={800}
                colorEncabezado="rgba(0, 159, 227, 0.03)"
                textoVacio="No hay solicitudes de traspaso."
                paginacion={{
                    count: data?.totalElementos ?? 0,
                    page: pagina,
                    rowsPerPage: 20,
                    onPageChange: (_, p) => setPagina(p),
                }}
            />

            {detalle && (
                <Dialog open onClose={() => setDetalle(null)} maxWidth="md" fullWidth fullScreen={esMovil}>
                    <DialogTitle sx={{ display: "flex", alignItems: "center", gap: 2, flexWrap: "wrap" }}>
                        <Box sx={{ flexGrow: 1 }}>
                            <Typography variant="caption" color="text.secondary">
                                SOLICITUD #{detalle.id} · FOLIO {formatoFolio(detalle)}
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 800 }}>
                                {detalle.areaOrigen.nombre} → {detalle.areaDestino.nombre}
                            </Typography>
                        </Box>
                        <EstatusTraspasoChip estatus={detalle.estatus} />
                    </DialogTitle>
                    <DialogContent dividers>
                        <Grid container spacing={2} sx={{ mb: 2 }}>
                            {[
                                ["Oficio de petición", detalle.oficioPeticion],
                                ["Solicitó", `${detalle.solicitante.nombreCompleto} · ${formatoFechaHora(detalle.fechaSolicitud)}`],
                                ["Recibe", [detalle.recibeNombre, detalle.recibeCargo].filter(Boolean).join(" — ")],
                                ["Motivo", detalle.motivo],
                                detalle.resueltoPor && [
                                    detalle.estatus === "CANCELADA" ? "Canceló" : "Resolvió",
                                    `${detalle.resueltoPor.nombreCompleto} · ${formatoFechaHora(detalle.fechaResolucion)}`,
                                ],
                                detalle.comentarioResolucion && ["Comentario", detalle.comentarioResolucion],
                            ]
                                .filter(Boolean)
                                .map(([k, v]) => (
                                    <Grid key={k} size={{ xs: 12, sm: 6 }}>
                                        <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>{k}</Typography>
                                        <Typography variant="body2" sx={{ fontWeight: 600, whiteSpace: "pre-wrap" }}>{v || "—"}</Typography>
                                    </Grid>
                                ))}
                        </Grid>
                        <TablaResponsiva
                            columnas={[
                                { id: "inv", label: "Inventario", render: (b) => b.noInventario, principal: true },
                                { id: "desc", label: "Descripción", render: (b) => b.descripcion, secundaria: true },
                                { id: "marca", label: "Marca / Modelo", render: (b) => [b.marca, b.modelo].filter(Boolean).join(" ") },
                                { id: "serie", label: "Serie", render: (b) => b.serie },
                                { id: "estado", label: "Estado", render: (b) => b.estado },
                                { id: "costo", label: "Costo", align: "right", render: (b) => formatoMoneda(b.costo) },
                            ]}
                            filas={detalle.bienes}
                            getKey={(b) => b.bienId}
                            minWidth={600}
                        />
                    </DialogContent>
                    <DialogActions sx={{ px: 3, py: 1.5 }}>
                        {botones(detalle)}
                        <Button onClick={() => setDetalle(null)}>Cerrar</Button>
                    </DialogActions>
                </Dialog>
            )}

            {accion && (
                <ConfirmDialog
                    key={`${accion.tipo}-${accion.traspaso.id}`}
                    open
                    titulo={ta.titulo}
                    mensaje={ta.mensaje}
                    textoConfirmar={ta.confirmar}
                    color={ta.color}
                    comentario={ta.comentario}
                    etiquetaComentario={accion.tipo === "rechazar" ? "Motivo del rechazo" : "Comentario"}
                    onConfirm={ejecutar}
                    onClose={() => setAccion(null)}
                />
            )}

            {nueva && (
                <SolicitarTraspasoDialog
                    key={nueva}
                    open
                    onClose={() => setNueva(null)}
                    onCreada={() => {
                        setEstatus("PENDIENTE");
                        setPagina(0);
                        recargar();
                        setAviso({ tipo: "success", mensaje: "Solicitud enviada a Patrimonio para su autorización." });
                    }}
                />
            )}

            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}
