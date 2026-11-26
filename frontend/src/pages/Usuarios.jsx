import { useState } from "react";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    IconButton,
    InputAdornment,
    MenuItem,
    Switch,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import PersonAddIcon from "@mui/icons-material/PersonAddAlt1";
import EditIcon from "@mui/icons-material/EditOutlined";
import KeyIcon from "@mui/icons-material/KeyOutlined";
import SearchIcon from "@mui/icons-material/Search";
import useAuth from "../auth/useAuth";
import { ROLES, ROL_INFO } from "../auth/roles";
import useFetch from "../hooks/useFetch";
import useDebounce from "../hooks/useDebounce";
import useEsMovil from "../hooks/useEsMovil";
import { usuariosApi } from "../api/endpoints";
import AreaSelect from "../components/AreaSelect";
import Aviso from "../components/Aviso";
import EncabezadoPagina from "../components/EncabezadoPagina";
import TablaResponsiva from "../components/TablaResponsiva";

export default function Usuarios() {
    const { usuario: yo } = useAuth();
    // Patrimonio solo gestiona usuarios de área; el backend limita lo que puede ver y asignar
    const esAdmin = yo.rol === ROLES.ADMINISTRADOR;
    const [busqueda, setBusqueda] = useState("");
    const [rol, setRol] = useState("");
    const [areaId, setAreaId] = useState("");
    const [pagina, setPagina] = useState(0);
    const [editor, setEditor] = useState(null); // { key, usuario | null }
    const [cambioPassword, setCambioPassword] = useState(null);
    const [aviso, setAviso] = useState(null);

    const q = useDebounce(busqueda);
    const { data, error, cargando, recargar } = useFetch("/admin/usuarios", {
        q,
        rol: esAdmin ? rol : "",
        areaId,
        page: pagina,
        size: 20,
    });

    const alternarActivo = async (u) => {
        try {
            await usuariosApi.cambiarActivo(u.id, !u.activo);
            recargar();
        } catch (e) {
            setAviso({ tipo: "error", mensaje: e.message });
        }
    };

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo={esAdmin ? "Usuarios y Roles" : "Usuarios de Área"}
                descripcion={
                    esAdmin
                        ? "Alta de usuarios, asignación de rol y área. Los usuarios de área solo ven el patrimonio de su área."
                        : "Alta y mantenimiento de los usuarios de cada área. Solo verán los bienes del área que les asignes."
                }
            >
                <Button variant="contained" startIcon={<PersonAddIcon />} onClick={() => setEditor({ key: Date.now(), usuario: null })}>
                    Nuevo usuario
                </Button>
            </EncabezadoPagina>

            {esAdmin && (
                <Grid container spacing={2}>
                    {Object.entries(ROL_INFO).map(([clave, info]) => (
                        <Grid key={clave} size={{ xs: 12, md: 4 }}>
                            <Card variant="outlined" sx={{ height: "100%" }}>
                                <CardContent>
                                    <Chip label={info.label} color={info.color} size="small" sx={{ fontWeight: 700, mb: 1 }} />
                                    <Typography variant="body2" color="text.secondary">{info.descripcion}</Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            )}

            <Card>
                <CardContent sx={{ py: 2, "&:last-child": { pb: 2 } }}>
                    <Grid container spacing={2}>
                        <Grid size={{ xs: 12, md: esAdmin ? 5 : 7 }}>
                            <TextField
                                fullWidth
                                placeholder="Buscar por nombre o correo…"
                                value={busqueda}
                                onChange={(e) => {
                                    setBusqueda(e.target.value);
                                    setPagina(0);
                                }}
                                slotProps={{
                                    input: {
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <SearchIcon color="action" />
                                            </InputAdornment>
                                        ),
                                    },
                                }}
                            />
                        </Grid>
                        {esAdmin && (
                            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                                <TextField
                                    select
                                    fullWidth
                                    label="Rol"
                                    value={rol}
                                    onChange={(e) => {
                                        setRol(e.target.value);
                                        setPagina(0);
                                    }}
                                >
                                    <MenuItem value="">Todos los roles</MenuItem>
                                    {Object.entries(ROL_INFO).map(([clave, info]) => (
                                        <MenuItem key={clave} value={clave}>{info.label}</MenuItem>
                                    ))}
                                </TextField>
                            </Grid>
                        )}
                        <Grid size={{ xs: 12, sm: esAdmin ? 6 : 12, md: esAdmin ? 4 : 5 }}>
                            <AreaSelect
                                label="Filtrar por área"
                                value={areaId}
                                onChange={(id) => {
                                    setAreaId(id);
                                    setPagina(0);
                                }}
                            />
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            {error && <Alert severity="error">{error.message}</Alert>}

            <TablaResponsiva
                columnas={[
                    { id: "nombre", label: "Nombre", render: (u) => u.nombreCompleto, principal: true },
                    { id: "correo", label: "Correo", render: (u) => u.correo, secundaria: true },
                    {
                        id: "rol",
                        label: "Rol",
                        render: (u) => (
                            <Chip label={ROL_INFO[u.rol]?.label ?? u.rol} color={ROL_INFO[u.rol]?.color} size="small" sx={{ fontWeight: 700 }} />
                        ),
                    },
                    {
                        id: "area",
                        label: "Área",
                        render: (u) =>
                            u.area ? (
                                <>
                                    {u.area.nombre}
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
                                        {u.area.direccionGeneral}
                                    </Typography>
                                </>
                            ) : (
                                "—"
                            ),
                    },
                    {
                        id: "activo",
                        label: "Activo",
                        render: (u) => (
                            <Switch
                                size="small"
                                checked={u.activo}
                                disabled={u.id === yo.id}
                                onClick={(e) => e.stopPropagation()}
                                onChange={() => alternarActivo(u)}
                            />
                        ),
                    },
                ]}
                filas={data?.contenido ?? []}
                cargando={cargando}
                minWidth={700}
                textoVacio="No se encontraron usuarios."
                acciones={(u) => (
                    <>
                        <Tooltip title="Editar">
                            <IconButton size="small" onClick={() => setEditor({ key: Date.now(), usuario: u })}>
                                <EditIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                        <Tooltip title="Cambiar contraseña">
                            <IconButton size="small" onClick={() => setCambioPassword(u)}>
                                <KeyIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                    </>
                )}
                paginacion={{
                    count: data?.totalElementos ?? 0,
                    page: pagina,
                    rowsPerPage: 20,
                    onPageChange: (_, p) => setPagina(p),
                }}
            />

            {editor && (
                <UsuarioDialog
                    key={editor.key}
                    usuario={editor.usuario}
                    esAdmin={esAdmin}
                    onClose={() => setEditor(null)}
                    onGuardado={(u) => {
                        recargar();
                        setAviso({ tipo: "success", mensaje: `Usuario ${u.correo} guardado.` });
                    }}
                />
            )}

            {cambioPassword && (
                <PasswordDialog
                    key={cambioPassword.id}
                    usuario={cambioPassword}
                    onClose={() => setCambioPassword(null)}
                    onGuardado={() => setAviso({ tipo: "success", mensaje: "Contraseña actualizada." })}
                />
            )}

            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}

function UsuarioDialog({ usuario, esAdmin, onClose, onGuardado }) {
    const esMovil = useEsMovil();
    const nuevo = !usuario;
    const [form, setForm] = useState({
        nombre: usuario?.nombre ?? "",
        apellidoPaterno: usuario?.apellidoPaterno ?? "",
        apellidoMaterno: usuario?.apellidoMaterno ?? "",
        correo: usuario?.correo ?? "",
        password: "",
        rol: usuario?.rol ?? ROLES.USUARIO,
        areaId: usuario?.area?.id ?? "",
    });
    const [errores, setErrores] = useState({});
    const [error, setError] = useState("");
    const [guardando, setGuardando] = useState(false);

    const set = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

    const guardar = async () => {
        setGuardando(true);
        setError("");
        setErrores({});
        const body = { ...form, areaId: form.areaId === "" ? null : Number(form.areaId) };
        try {
            const res = nuevo ? await usuariosApi.crear(body) : await usuariosApi.editar(usuario.id, body);
            onGuardado(res);
            onClose();
        } catch (e) {
            setError(e.message);
            setErrores(e.campos ?? {});
        } finally {
            setGuardando(false);
        }
    };

    const campo = (name, label, extra = {}) => (
        <TextField
            fullWidth
            label={label}
            value={form[name]}
            onChange={set(name)}
            error={Boolean(errores[name])}
            helperText={errores[name]}
            {...extra}
        />
    );

    return (
        <Dialog open onClose={guardando ? undefined : onClose} maxWidth="sm" fullWidth fullScreen={esMovil}>
            <DialogTitle sx={{ fontWeight: 800 }}>
                {nuevo ? (esAdmin ? "Nuevo usuario" : "Nuevo usuario de área") : "Editar usuario"}
            </DialogTitle>
            <DialogContent dividers>
                <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 4 }}>{campo("nombre", "Nombre(s)", { required: true })}</Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>{campo("apellidoPaterno", "Apellido paterno", { required: true })}</Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>{campo("apellidoMaterno", "Apellido materno")}</Grid>
                    <Grid size={12}>{campo("correo", "Correo electrónico", { required: true, type: "email" })}</Grid>
                    {nuevo && (
                        <Grid size={12}>
                            {campo("password", "Contraseña inicial", {
                                required: true,
                                type: "password",
                                autoComplete: "new-password",
                                helperText: errores.password ?? "Mínimo 8 caracteres",
                            })}
                        </Grid>
                    )}
                    {esAdmin && (
                        <Grid size={{ xs: 12, sm: 5 }}>
                            <TextField select fullWidth required label="Rol" value={form.rol} onChange={set("rol")}>
                                {Object.entries(ROL_INFO).map(([clave, info]) => (
                                    <MenuItem key={clave} value={clave}>{info.label}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                    )}
                    <Grid size={{ xs: 12, sm: esAdmin ? 7 : 12 }}>
                        <AreaSelect
                            label="Área"
                            required={form.rol === ROLES.USUARIO}
                            value={form.areaId}
                            onChange={(id) => setForm((f) => ({ ...f, areaId: id }))}
                            helperText={form.rol === ROLES.USUARIO ? "Determina qué bienes podrá ver" : "Opcional"}
                        />
                    </Grid>
                </Grid>
                {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 1.5 }}>
                <Button onClick={onClose} disabled={guardando}>Cancelar</Button>
                <Button variant="contained" onClick={guardar} disabled={guardando}>
                    {guardando ? "Guardando…" : "Guardar"}
                </Button>
            </DialogActions>
        </Dialog>
    );
}

function PasswordDialog({ usuario, onClose, onGuardado }) {
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [guardando, setGuardando] = useState(false);

    const guardar = async () => {
        setGuardando(true);
        setError("");
        try {
            await usuariosApi.cambiarPassword(usuario.id, password);
            onGuardado();
            onClose();
        } catch (e) {
            setError(e.campos?.password ?? e.message);
        } finally {
            setGuardando(false);
        }
    };

    return (
        <Dialog open onClose={guardando ? undefined : onClose} maxWidth="xs" fullWidth>
            <DialogTitle sx={{ fontWeight: 800 }}>Cambiar contraseña</DialogTitle>
            <DialogContent>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2, wordBreak: "break-word" }}>
                    {usuario.nombreCompleto} ({usuario.correo})
                </Typography>
                <TextField
                    fullWidth
                    autoFocus
                    type="password"
                    autoComplete="new-password"
                    label="Nueva contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    helperText="Mínimo 8 caracteres"
                />
                {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2 }}>
                <Button onClick={onClose} disabled={guardando}>Cancelar</Button>
                <Button variant="contained" onClick={guardar} disabled={guardando || password.length < 8}>
                    Guardar
                </Button>
            </DialogActions>
        </Dialog>
    );
}
