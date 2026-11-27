import { useState } from "react";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemText,
    ListSubheader,
    MenuItem,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import BusinessIcon from "@mui/icons-material/Business";
import AccountTreeIcon from "@mui/icons-material/AccountTreeOutlined";
import DrawIcon from "@mui/icons-material/DrawOutlined";
import EditIcon from "@mui/icons-material/EditOutlined";
import DeleteIcon from "@mui/icons-material/DeleteOutlined";
import AddIcon from "@mui/icons-material/Add";
import useFetch from "../hooks/useFetch";
import { catalogosApi, parametrosApi } from "../api/endpoints";
import Aviso from "../components/Aviso";
import ConfirmDialog from "../components/ConfirmDialog";
import ConfigDepreciacion from "../components/ConfigDepreciacion";
import EncabezadoPagina from "../components/EncabezadoPagina";
import useAuth from "../auth/useAuth";
import { ROLES } from "../auth/roles";
import useEsMovil from "../hooks/useEsMovil";

export default function Configuracion() {
    // Patrimonio solo administra áreas; direcciones generales y firmantes son del administrador
    const { usuario } = useAuth();
    const esAdmin = usuario.rol === ROLES.ADMINISTRADOR;
    const direcciones = useFetch("/catalogos/direcciones");
    const areas = useFetch("/catalogos/areas");
    const [editor, setEditor] = useState(null); // { key, tipo: "direccion" | "area", item }
    const [eliminar, setEliminar] = useState(null); // { tipo, item }
    const [aviso, setAviso] = useState(null);

    const recargarTodo = () => {
        direcciones.recargar();
        areas.recargar();
    };

    const confirmarEliminar = async () => {
        if (eliminar.tipo === "direccion") await catalogosApi.eliminarDireccion(eliminar.item.id);
        else await catalogosApi.eliminarArea(eliminar.item.id);
        recargarTodo();
        setAviso({ tipo: "success", mensaje: `"${eliminar.item.nombre}" eliminado.` });
    };

    const areasPorDireccion = (direcciones.data ?? []).map((d) => ({
        direccion: d,
        areas: (areas.data ?? []).filter((a) => a.direccionGeneral.id === d.id),
    }));

    const botonesItem = (tipo, item) => (
        <>
            <Tooltip title="Editar">
                <IconButton size="small" onClick={() => setEditor({ key: Date.now(), tipo, item })}>
                    <EditIcon fontSize="small" />
                </IconButton>
            </Tooltip>
            <Tooltip title="Eliminar">
                <IconButton size="small" color="error" onClick={() => setEliminar({ tipo, item })}>
                    <DeleteIcon fontSize="small" />
                </IconButton>
            </Tooltip>
        </>
    );

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo={esAdmin ? "Configuración del Sistema" : "Áreas"}
                descripcion={
                    esAdmin
                        ? "Estructura municipal (direcciones generales y áreas), firmantes del vale de traspaso y reglas de depreciación."
                        : "Registro de las áreas de cada dirección general. Las direcciones generales las da de alta el administrador."
                }
            />

            {(direcciones.error || areas.error) && (
                <Alert severity="error">{(direcciones.error || areas.error).message}</Alert>
            )}

            <Grid container spacing={3}>
                {esAdmin && (
                <Grid size={{ xs: 12, md: 5 }}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="subtitle1" sx={{ fontWeight: "700", mb: 1, color: "primary.main", display: "flex", alignItems: "center", gap: 1 }}>
                                <BusinessIcon />
                                Direcciones generales
                            </Typography>
                            <List dense>
                                {(direcciones.data ?? []).map((d) => (
                                    <ListItem key={d.id} divider secondaryAction={botonesItem("direccion", d)} sx={{ pr: 11 }}>
                                        <ListItemText primary={d.nombre} />
                                    </ListItem>
                                ))}
                                {direcciones.data?.length === 0 && (
                                    <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                                        Aún no hay direcciones registradas.
                                    </Typography>
                                )}
                            </List>
                            <Button fullWidth variant="outlined" startIcon={<AddIcon />} sx={{ mt: 2 }} onClick={() => setEditor({ key: Date.now(), tipo: "direccion", item: null })}>
                                Agregar dirección general
                            </Button>
                        </CardContent>
                    </Card>
                </Grid>
                )}

                <Grid size={{ xs: 12, md: esAdmin ? 7 : 12 }}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="subtitle1" sx={{ fontWeight: "700", mb: 1, color: "secondary.main", display: "flex", alignItems: "center", gap: 1 }}>
                                <AccountTreeIcon />
                                Áreas
                            </Typography>
                            <List dense sx={{ maxHeight: 420, overflowY: "auto" }}>
                                {areasPorDireccion.map(({ direccion, areas: lista }) => (
                                    <li key={direccion.id}>
                                        <ul style={{ padding: 0 }}>
                                            <ListSubheader sx={{ fontWeight: 700, lineHeight: "32px" }}>{direccion.nombre}</ListSubheader>
                                            {lista.map((a) => (
                                                <ListItem key={a.id} divider secondaryAction={botonesItem("area", a)} sx={{ pr: 11 }}>
                                                    <ListItemText primary={a.nombre} secondary={a.nombreCargo ? `Titular: ${a.nombreCargo}` : "Sin titular registrado"} />
                                                </ListItem>
                                            ))}
                                            {lista.length === 0 && (
                                                <ListItem>
                                                    <ListItemText secondary="Sin áreas" />
                                                </ListItem>
                                            )}
                                        </ul>
                                    </li>
                                ))}
                            </List>
                            <Button
                                fullWidth
                                variant="outlined"
                                color="secondary"
                                startIcon={<AddIcon />}
                                sx={{ mt: 2 }}
                                disabled={!direcciones.data?.length}
                                onClick={() => setEditor({ key: Date.now(), tipo: "area", item: null })}
                            >
                                Agregar área
                            </Button>
                            {direcciones.data?.length === 0 && (
                                <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 1 }}>
                                    {esAdmin
                                        ? "Primero registra una dirección general."
                                        : "Aún no hay direcciones generales. Solicita al administrador que las registre."}
                                </Typography>
                            )}
                        </CardContent>
                    </Card>
                </Grid>

                {esAdmin && (
                    <Grid size={12}>
                        <FirmantesVale onGuardado={() => setAviso({ tipo: "success", mensaje: "Firmantes del vale actualizados." })} />
                    </Grid>
                )}

                {esAdmin && (
                    <Grid size={12}>
                        <ConfigDepreciacion onAviso={setAviso} />
                    </Grid>
                )}
            </Grid>

            {editor && (
                <CatalogoDialog
                    key={editor.key}
                    tipo={editor.tipo}
                    item={editor.item}
                    direcciones={direcciones.data ?? []}
                    onClose={() => setEditor(null)}
                    onGuardado={() => {
                        recargarTodo();
                        setAviso({ tipo: "success", mensaje: "Cambios guardados." });
                    }}
                />
            )}

            {eliminar && (
                <ConfirmDialog
                    key={`${eliminar.tipo}-${eliminar.item.id}`}
                    open
                    titulo="Eliminar registro"
                    mensaje={`¿Eliminar "${eliminar.item.nombre}"? Solo es posible si no tiene áreas, usuarios ni bienes asociados.`}
                    textoConfirmar="Eliminar"
                    color="error"
                    onConfirm={confirmarEliminar}
                    onClose={() => setEliminar(null)}
                />
            )}

            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}

function CatalogoDialog({ tipo, item, direcciones, onClose, onGuardado }) {
    const esMovil = useEsMovil();
    const esArea = tipo === "area";
    const [form, setForm] = useState({
        nombre: item?.nombre ?? "",
        nombreCargo: item?.nombreCargo ?? "",
        direccionGeneralId: item?.direccionGeneral?.id ?? direcciones[0]?.id ?? "",
    });
    const [error, setError] = useState("");
    const [guardando, setGuardando] = useState(false);

    const guardar = async () => {
        setGuardando(true);
        setError("");
        try {
            if (esArea) {
                const body = { ...form, direccionGeneralId: Number(form.direccionGeneralId) };
                if (item) await catalogosApi.editarArea(item.id, body);
                else await catalogosApi.crearArea(body);
            } else {
                const body = { nombre: form.nombre };
                if (item) await catalogosApi.editarDireccion(item.id, body);
                else await catalogosApi.crearDireccion(body);
            }
            onGuardado();
            onClose();
        } catch (e) {
            setError(e.campos ? Object.values(e.campos).join(". ") : e.message);
        } finally {
            setGuardando(false);
        }
    };

    return (
        <Dialog open onClose={guardando ? undefined : onClose} maxWidth="sm" fullWidth fullScreen={esMovil}>
            <DialogTitle sx={{ fontWeight: 800 }}>
                {item ? "Editar" : "Nueva"} {esArea ? "área" : "dirección general"}
            </DialogTitle>
            <DialogContent dividers sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                {esArea && (
                    <TextField
                        select
                        fullWidth
                        required
                        label="Dirección general"
                        value={form.direccionGeneralId}
                        onChange={(e) => setForm((f) => ({ ...f, direccionGeneralId: e.target.value }))}
                    >
                        {direcciones.map((d) => (
                            <MenuItem key={d.id} value={d.id}>{d.nombre}</MenuItem>
                        ))}
                    </TextField>
                )}
                <TextField
                    fullWidth
                    required
                    autoFocus
                    label={esArea ? "Nombre del área" : "Nombre de la dirección general"}
                    value={form.nombre}
                    onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
                />
                {esArea && (
                    <TextField
                        fullWidth
                        label="Titular del área (nombre completo)"
                        helperText="Responsable del inventario a su resguardo; firma los vales de traspaso"
                        value={form.nombreCargo}
                        onChange={(e) => setForm((f) => ({ ...f, nombreCargo: e.target.value }))}
                    />
                )}
                {error && <Alert severity="error">{error}</Alert>}
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 1.5 }}>
                <Button onClick={onClose} disabled={guardando}>Cancelar</Button>
                <Button variant="contained" onClick={guardar} disabled={guardando || form.nombre.trim() === ""}>
                    Guardar
                </Button>
            </DialogActions>
        </Dialog>
    );
}

function FirmantesVale({ onGuardado }) {
    const { data: todos, error, recargar } = useFetch("/admin/parametros");
    // Solo los firmantes; la regla de depreciación tiene su propia sección
    const data = todos?.filter((p) => p.clave.startsWith("firma."));
    return (
        <Card>
            <CardContent>
                <Typography variant="subtitle1" sx={{ fontWeight: "700", mb: 0.5, color: "info.main", display: "flex", alignItems: "center", gap: 1 }}>
                    <DrawIcon />
                    Firmantes del vale de traspaso
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Nombre y cargo de quien autoriza cada vale (Dirección de Patrimonio). Las firmas de entrega y recibe salen de cada traspaso.
                </Typography>
                {error && <Alert severity="error">{error.message}</Alert>}
                {data && (
                    <FormularioFirmantes
                        // Se reinicia cuando cambian los datos guardados
                        key={JSON.stringify(data)}
                        parametros={data}
                        onGuardado={() => {
                            recargar();
                            onGuardado();
                        }}
                    />
                )}
            </CardContent>
        </Card>
    );
}

function FormularioFirmantes({ parametros, onGuardado }) {
    const [valores, setValores] = useState(() => Object.fromEntries(parametros.map((p) => [p.clave, p.valor ?? ""])));
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState("");

    const guardar = async () => {
        setGuardando(true);
        setError("");
        try {
            await parametrosApi.guardar(parametros.map((p) => ({ clave: p.clave, valor: valores[p.clave] })));
            onGuardado();
        } catch (e) {
            setError(e.message);
            setGuardando(false);
        }
    };

    return (
        <>
            <Grid container spacing={2}>
                {parametros.map((p) => (
                    <Grid key={p.clave} size={{ xs: 12, md: 6 }}>
                        <TextField
                            fullWidth
                            label={p.descripcion ?? p.clave}
                            value={valores[p.clave]}
                            onChange={(e) => setValores((v) => ({ ...v, [p.clave]: e.target.value }))}
                        />
                    </Grid>
                ))}
            </Grid>
            {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
                <Button variant="contained" color="info" onClick={guardar} disabled={guardando}>
                    {guardando ? "Guardando…" : "Guardar firmantes"}
                </Button>
            </Box>
        </>
    );
}
