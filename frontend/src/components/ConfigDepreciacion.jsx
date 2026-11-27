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
    FormControlLabel,
    IconButton,
    MenuItem,
    Switch,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import TrendingDownIcon from "@mui/icons-material/TrendingDown";
import EditIcon from "@mui/icons-material/EditOutlined";
import AddIcon from "@mui/icons-material/Add";
import useFetch from "../hooks/useFetch";
import useEsMovil from "../hooks/useEsMovil";
import { cuentasContablesApi, parametrosApi } from "../api/endpoints";
import { CLAVE_REGLA, REGLAS_DEPRECIACION, TIPOS_CONTABLES, etiquetaTipoContable } from "../bienes/depreciacion";
import TablaResponsiva from "./TablaResponsiva";

/** Configuración de la depreciación (solo ADMINISTRADOR): regla de inicio y catálogo de cuentas CONAC. */
export default function ConfigDepreciacion({ onAviso }) {
    const parametros = useFetch("/admin/parametros");
    const cuentas = useFetch("/admin/cuentas-contables");
    const [editor, setEditor] = useState(null); // { key, cuenta | null }
    const reglaGuardada = parametros.data?.find((p) => p.clave === CLAVE_REGLA)?.valor ?? "";

    return (
        <Card>
            <CardContent>
                <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: "700", mb: 0.5, color: "secondary.main", display: "flex", alignItems: "center", gap: 1 }}
                >
                    <TrendingDownIcon />
                    Depreciación (Guía de Vida Útil CONAC)
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Línea recta: % anual = 100 ÷ años de vida útil. Cada bien se deprecia hasta quedar con un valor de $1,
                    para que siga en el inventario mientras se use.
                </Typography>

                {(parametros.error || cuentas.error) && (
                    <Alert severity="error" sx={{ mb: 2 }}>{(parametros.error || cuentas.error).message}</Alert>
                )}

                {parametros.data && (
                    <FormularioRegla
                        key={reglaGuardada}
                        reglaGuardada={reglaGuardada}
                        onGuardado={() => {
                            parametros.recargar();
                            onAviso({ tipo: "success", mensaje: "Regla de depreciación actualizada. Se aplica a todos los bienes." });
                        }}
                    />
                )}

                <Box sx={{ display: "flex", alignItems: "center", gap: 1, flexWrap: "wrap", mt: 3, mb: 1.5 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, flexGrow: 1 }}>
                        Cuentas contables y vida útil
                    </Typography>
                    <Button size="small" variant="outlined" color="secondary" startIcon={<AddIcon />} onClick={() => setEditor({ key: Date.now(), cuenta: null })}>
                        Agregar cuenta
                    </Button>
                </Box>
                <TablaResponsiva
                    cargando={cuentas.cargando}
                    minWidth={640}
                    filas={cuentas.data ?? []}
                    textoVacio="Sin cuentas registradas"
                    columnas={[
                        { id: "clave", label: "Cuenta", render: (c) => c.clave, principal: true },
                        { id: "concepto", label: "Concepto", render: (c) => c.concepto, secundaria: true },
                        { id: "tipo", label: "Tipo de bien", render: (c) => etiquetaTipoContable(c.tipoBien) },
                        {
                            id: "vida",
                            label: "Vida útil",
                            align: "right",
                            render: (c) =>
                                c.vidaUtilAnios ? (
                                    <Box component="span" sx={{ whiteSpace: "nowrap" }}>
                                        {c.vidaUtilAnios} años · {c.porcentajeAnual} %
                                    </Box>
                                ) : (
                                    <Chip size="small" color="warning" label="Sin definir" />
                                ),
                        },
                        {
                            id: "estado",
                            label: "Estado",
                            render: (c) =>
                                c.activo ? <Chip size="small" color="success" label="Activa" /> : <Chip size="small" label="Inactiva" />,
                        },
                    ]}
                    acciones={(c) => (
                        <Tooltip title="Editar">
                            <IconButton size="small" onClick={() => setEditor({ key: Date.now(), cuenta: c })}>
                                <EditIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                    )}
                />
            </CardContent>

            {editor && (
                <CuentaDialog
                    key={editor.key}
                    cuenta={editor.cuenta}
                    onClose={() => setEditor(null)}
                    onGuardado={() => {
                        cuentas.recargar();
                        onAviso({ tipo: "success", mensaje: "Cuenta contable guardada." });
                    }}
                />
            )}
        </Card>
    );
}

function FormularioRegla({ reglaGuardada, onGuardado }) {
    const [regla, setRegla] = useState(reglaGuardada || "ANIO_SIGUIENTE");
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState("");
    const seleccion = REGLAS_DEPRECIACION.find((r) => r.valor === regla);

    const guardar = async () => {
        setGuardando(true);
        setError("");
        try {
            await parametrosApi.guardar([{ clave: CLAVE_REGLA, valor: regla }]);
            onGuardado();
        } catch (e) {
            setError(e.message);
            setGuardando(false);
        }
    };

    return (
        <Box sx={{ display: "flex", flexDirection: { xs: "column", sm: "row" }, gap: 2, alignItems: { sm: "flex-start" } }}>
            <TextField
                select
                fullWidth
                label="¿Desde cuándo se empieza a depreciar?"
                value={regla}
                onChange={(e) => setRegla(e.target.value)}
                helperText={seleccion?.ejemplo}
                sx={{ maxWidth: { sm: 560 } }}
            >
                {REGLAS_DEPRECIACION.map((r) => (
                    <MenuItem key={r.valor} value={r.valor}>{r.label}</MenuItem>
                ))}
            </TextField>
            <Button variant="contained" color="secondary" onClick={guardar} disabled={guardando || regla === reglaGuardada}>
                {guardando ? "Guardando…" : "Guardar regla"}
            </Button>
            {error && <Alert severity="error">{error}</Alert>}
        </Box>
    );
}

function CuentaDialog({ cuenta, onClose, onGuardado }) {
    const esMovil = useEsMovil();
    const [form, setForm] = useState({
        clave: cuenta?.clave ?? "",
        concepto: cuenta?.concepto ?? "",
        tipoBien: cuenta?.tipoBien ?? "MUEBLE",
        vidaUtilAnios: cuenta?.vidaUtilAnios ?? "",
        activo: cuenta?.activo ?? true,
    });
    const [error, setError] = useState("");
    const [guardando, setGuardando] = useState(false);
    const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
    const vida = Number(form.vidaUtilAnios);

    const guardar = async () => {
        setGuardando(true);
        setError("");
        const body = { ...form, vidaUtilAnios: form.vidaUtilAnios === "" ? null : vida };
        try {
            if (cuenta) await cuentasContablesApi.actualizar(cuenta.id, body);
            else await cuentasContablesApi.crear(body);
            onGuardado();
            onClose();
        } catch (e) {
            setError(e.campos ? Object.values(e.campos).join(". ") : e.message);
            setGuardando(false);
        }
    };

    return (
        <Dialog open onClose={guardando ? undefined : onClose} maxWidth="sm" fullWidth fullScreen={esMovil}>
            <DialogTitle sx={{ fontWeight: 800 }}>{cuenta ? `Cuenta ${cuenta.clave}` : "Nueva cuenta contable"}</DialogTitle>
            <DialogContent dividers sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                {cuenta && (
                    <Alert severity="info">
                        La clave y el tipo de bien no se pueden cambiar. Si cambias la vida útil, se recalcula la depreciación de
                        todos los bienes de esta cuenta.
                    </Alert>
                )}
                <TextField
                    required
                    label="Clave (p. ej. 1.2.4.1.3)"
                    value={form.clave}
                    disabled={Boolean(cuenta)}
                    onChange={(e) => set("clave", e.target.value)}
                />
                <TextField required label="Concepto" value={form.concepto} onChange={(e) => set("concepto", e.target.value)} />
                <TextField
                    select
                    required
                    label="Tipo de bien"
                    value={form.tipoBien}
                    disabled={Boolean(cuenta)}
                    onChange={(e) => set("tipoBien", e.target.value)}
                >
                    {TIPOS_CONTABLES.map((t) => (
                        <MenuItem key={t.valor} value={t.valor}>{t.label}</MenuItem>
                    ))}
                </TextField>
                <TextField
                    type="number"
                    label="Años de vida útil"
                    value={form.vidaUtilAnios}
                    onChange={(e) => set("vidaUtilAnios", e.target.value)}
                    slotProps={{ htmlInput: { min: 1, max: 100, step: 1 } }}
                    helperText={
                        vida > 0
                            ? `Depreciación anual: ${(100 / vida).toFixed(2)} %`
                            : "Sin vida útil, la cuenta no se puede asignar a bienes."
                    }
                />
                <FormControlLabel
                    control={<Switch checked={form.activo} onChange={(e) => set("activo", e.target.checked)} />}
                    label="Activa (se ofrece al dar de alta bienes)"
                />
                {error && <Alert severity="error">{error}</Alert>}
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 1.5 }}>
                <Button onClick={onClose} disabled={guardando}>Cancelar</Button>
                <Button
                    variant="contained"
                    onClick={guardar}
                    disabled={guardando || !form.clave.trim() || !form.concepto.trim()}
                >
                    Guardar
                </Button>
            </DialogActions>
        </Dialog>
    );
}
