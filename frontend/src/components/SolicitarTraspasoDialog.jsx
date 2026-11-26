import { useState } from "react";
import {
    Alert,
    Autocomplete,
    Box,
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    TextField,
    Typography,
} from "@mui/material";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import AreaSelect from "./AreaSelect";
import useFetch from "../hooks/useFetch";
import useDebounce from "../hooks/useDebounce";
import useEsMovil from "../hooks/useEsMovil";
import { traspasosApi } from "../api/endpoints";

/**
 * Solicitud de traspaso de bienes muebles a otra área.
 * Todos los bienes deben pertenecer a la misma área (para un USUARIO, siempre la suya).
 * Montar con `key` distinta por apertura para reiniciar el formulario.
 */
export default function SolicitarTraspasoDialog({ open, onClose, onCreada, bienesIniciales = [] }) {
    const esMovil = useEsMovil();
    const [bienes, setBienes] = useState(bienesIniciales);
    const [busqueda, setBusqueda] = useState("");
    const [form, setForm] = useState({ areaDestinoId: "", oficioPeticion: "", motivo: "", recibeNombre: "", recibeCargo: "" });
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState(null);

    const q = useDebounce(busqueda);
    const { data, cargando } = useFetch(open ? "/bienes/muebles" : null, { q, size: 20 });

    // Una vez elegido el primer bien, solo se ofrecen bienes de la misma área
    const areaOrigen = bienes[0]?.area ?? null;
    const opciones = (data?.contenido ?? []).filter((b) => !areaOrigen || b.area.id === areaOrigen.id);

    const set = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

    const enviar = async () => {
        setEnviando(true);
        setError(null);
        try {
            const creada = await traspasosApi.solicitar({
                bienIds: bienes.map((b) => b.id),
                areaDestinoId: form.areaDestinoId === "" ? null : Number(form.areaDestinoId),
                oficioPeticion: form.oficioPeticion,
                motivo: form.motivo,
                recibeNombre: form.recibeNombre,
                recibeCargo: form.recibeCargo,
            });
            onCreada?.(creada);
            onClose();
        } catch (e) {
            setError(e);
        } finally {
            setEnviando(false);
        }
    };

    const valido = bienes.length > 0 && form.areaDestinoId !== "" && form.recibeNombre.trim() !== "";

    return (
        <Dialog open={open} onClose={enviando ? undefined : onClose} maxWidth="md" fullWidth fullScreen={esMovil}>
            <DialogTitle sx={{ fontWeight: 800, display: "flex", alignItems: "center", gap: 1 }}>
                <SwapHorizIcon color="info" /> Solicitar traspaso de bienes
            </DialogTitle>
            <DialogContent dividers>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    La solicitud se enviará a la Dirección de Patrimonio para su autorización. Al autorizarse, los bienes
                    cambian de área y se genera el vale de traspaso.
                </Typography>

                <Grid container spacing={2}>
                    <Grid size={12}>
                        <Autocomplete
                            multiple
                            options={opciones}
                            loading={cargando}
                            value={bienes}
                            filterOptions={(x) => x}
                            onChange={(_, v) => setBienes(v)}
                            inputValue={busqueda}
                            onInputChange={(_, v, motivo) => motivo !== "reset" && setBusqueda(v)}
                            getOptionLabel={(b) => `${b.noControlInventario} — ${b.descripcion}`}
                            isOptionEqualToValue={(a, b) => a.id === b.id}
                            noOptionsText={q ? "Sin coincidencias" : "Escribe para buscar"}
                            renderOption={(props, b) => {
                                const { key, ...rest } = props;
                                return (
                                    <li key={key} {...rest}>
                                        <Box>
                                            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                                {b.noControlInventario} — {b.descripcion}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                {[b.marca, b.modelo, b.serie].filter(Boolean).join(" · ")} · {b.area.nombre}
                                            </Typography>
                                        </Box>
                                    </li>
                                );
                            }}
                            renderValue={(valores, getItemProps) =>
                                valores.map((b, index) => {
                                    const { key, ...rest } = getItemProps({ index });
                                    return <Chip key={key} label={b.noControlInventario} size="small" {...rest} />;
                                })
                            }
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    required
                                    label="Bienes muebles a traspasar"
                                    placeholder="Buscar por inventario, descripción, marca o serie"
                                    helperText={areaOrigen ? `Área actual de los bienes: ${areaOrigen.nombre}` : " "}
                                />
                            )}
                        />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <AreaSelect
                            label="Área destino"
                            required
                            value={form.areaDestinoId}
                            excluirId={areaOrigen?.id}
                            onChange={(id) => setForm((f) => ({ ...f, areaDestinoId: id }))}
                        />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <TextField fullWidth label="Oficio de petición número" value={form.oficioPeticion} onChange={set("oficioPeticion")} placeholder="DPVXOCHI/595/06-2026" />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <TextField fullWidth required label="Nombre de quien recibe" value={form.recibeNombre} onChange={set("recibeNombre")} helperText="Servidor público del área destino que tendrá el resguardo" />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <TextField fullWidth label="Cargo de quien recibe" value={form.recibeCargo} onChange={set("recibeCargo")} />
                    </Grid>
                    <Grid size={12}>
                        <TextField fullWidth multiline minRows={2} label="Motivo del traspaso" value={form.motivo} onChange={set("motivo")} />
                    </Grid>
                </Grid>

                {error && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        {error.message}
                        {error.campos && (
                            <Box component="ul" sx={{ m: 0, pl: 2 }}>
                                {Object.values(error.campos).map((m) => (
                                    <li key={m}>{m}</li>
                                ))}
                            </Box>
                        )}
                    </Alert>
                )}
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 1.5 }}>
                <Button onClick={onClose} disabled={enviando}>
                    Cancelar
                </Button>
                <Button variant="contained" color="info" onClick={enviar} disabled={!valido || enviando} startIcon={<SwapHorizIcon />}>
                    {enviando ? "Enviando…" : "Enviar solicitud"}
                </Button>
            </DialogActions>
        </Dialog>
    );
}
