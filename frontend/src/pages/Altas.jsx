import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Divider,
    Grid,
    InputAdornment,
    MenuItem,
    Tab,
    Tabs,
    TextField,
    Typography,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import ClearIcon from "@mui/icons-material/Clear";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import useFetch from "../hooks/useFetch";
import { bienesApi } from "../api/endpoints";
import { CLAVES_TIPO, TIPOS_BIEN, aFormulario, aPeticion, formularioVacio } from "../bienes/tiposBien";
import AreaSelect from "../components/AreaSelect";
import Aviso from "../components/Aviso";
import EncabezadoPagina from "../components/EncabezadoPagina";

export default function Altas() {
    const [searchParams, setSearchParams] = useSearchParams();
    const tipoParam = searchParams.get("tipo");
    const tipo = CLAVES_TIPO.includes(tipoParam) ? tipoParam : "muebles";
    const id = searchParams.get("id");

    const { data: bien, error, cargando } = useFetch(id ? `/bienes/${tipo}/${id}` : null);
    const editando = Boolean(id);

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo={editando ? "Editar bien" : "Alta de Bien Municipal"}
                descripcion="Captura la información según la relación oficial de cada tipo de bien."
            />

            <Card>
                <Tabs
                    value={tipo}
                    onChange={(_, t) => setSearchParams({ tipo: t })}
                    sx={{ px: { xs: 0, sm: 2 }, borderBottom: 1, borderColor: "divider" }}
                >
                    {CLAVES_TIPO.map((t) => (
                        <Tab key={t} value={t} label={TIPOS_BIEN[t].label} disabled={editando && t !== tipo} sx={{ fontWeight: 700 }} />
                    ))}
                </Tabs>
                <CardContent sx={{ p: { xs: 2.5, md: 4 } }}>
                    {error && <Alert severity="error">{error.message}</Alert>}
                    {editando && cargando && !bien && (
                        <Box sx={{ display: "grid", placeItems: "center", py: 6 }}>
                            <CircularProgress />
                        </Box>
                    )}
                    {(!editando || (bien && !error)) && (
                        <FormularioBien
                            // El key reinicia el formulario al cambiar de tipo o de bien
                            key={`${tipo}-${id ?? "nuevo"}-${bien?.actualizadoEn ?? ""}`}
                            tipo={tipo}
                            bienId={id}
                            inicial={editando ? aFormulario(tipo, bien) : formularioVacio(tipo)}
                        />
                    )}
                </CardContent>
            </Card>
        </Box>
    );
}

function FormularioBien({ tipo, bienId, inicial }) {
    const navigate = useNavigate();
    const def = TIPOS_BIEN[tipo];
    const [form, setForm] = useState(inicial);
    const [errores, setErrores] = useState({});
    const [errorGeneral, setErrorGeneral] = useState("");
    const [guardando, setGuardando] = useState(false);
    const [aviso, setAviso] = useState(null);

    const set = (name, value) => setForm((f) => ({ ...f, [name]: value }));

    const guardar = async (e) => {
        e.preventDefault();
        setGuardando(true);
        setErrores({});
        setErrorGeneral("");
        try {
            const body = aPeticion(tipo, form);
            if (bienId) {
                await bienesApi.actualizar(tipo, bienId, body);
                navigate(`/dashboard/inventario?tipo=${tipo}`);
                return;
            }
            const creado = await bienesApi.crear(tipo, body);
            setAviso({ tipo: "success", mensaje: `Se registró el ${def.singular} ${creado[def.clave]}.` });
            setForm(formularioVacio(tipo));
        } catch (err) {
            setErrores(err.campos ?? {});
            setErrorGeneral(err.message);
        } finally {
            setGuardando(false);
        }
    };

    const renderCampo = (c) => {
        const comun = {
            fullWidth: true,
            label: c.label,
            required: c.required,
            value: form[c.name] ?? "",
            onChange: (e) => set(c.name, e.target.value),
            error: Boolean(errores[c.name]),
            helperText: errores[c.name] ?? c.ayuda,
            placeholder: c.placeholder,
        };
        if (c.opciones) {
            return (
                <TextField {...comun} select>
                    <MenuItem value="">
                        <em>Sin especificar</em>
                    </MenuItem>
                    {c.opciones.map((o) => (
                        <MenuItem key={o} value={o}>
                            {o}
                        </MenuItem>
                    ))}
                </TextField>
            );
        }
        switch (c.type) {
            case "textarea":
                return <TextField {...comun} multiline minRows={2} />;
            case "date":
                return <TextField {...comun} type="date" slotProps={{ inputLabel: { shrink: true } }} />;
            case "number":
                return <TextField {...comun} type="number" slotProps={{ htmlInput: { step: "any" } }} />;
            case "money":
                return (
                    <TextField
                        {...comun}
                        type="number"
                        slotProps={{
                            htmlInput: { step: "0.01", min: 0 },
                            input: { startAdornment: <InputAdornment position="start">$</InputAdornment> },
                        }}
                    />
                );
            default:
                return <TextField {...comun} />;
        }
    };

    return (
        <Box component="form" onSubmit={guardar} sx={{ display: "flex", flexDirection: "column", gap: 3.5 }}>
            <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: "700", color: "primary.main", mb: 2 }}>
                    Área de adscripción
                </Typography>
                <AreaSelect
                    label={tipo === "vehiculos" ? "Área designada" : "Área"}
                    required
                    value={form.areaId}
                    onChange={(v) => set("areaId", v)}
                    error={Boolean(errores.areaId)}
                    helperText={errores.areaId}
                    sx={{ maxWidth: { sm: 560 } }}
                />
            </Box>

            {def.secciones.map((s) => (
                <Box key={s.titulo}>
                    <Divider sx={{ mb: 3 }} />
                    <Typography variant="subtitle1" sx={{ fontWeight: "700", color: "primary.main", mb: 2 }}>
                        {s.titulo}
                    </Typography>
                    <Grid container spacing={2.5}>
                        {s.campos.map((c) => (
                            <Grid key={c.name} size={{ xs: 12, sm: c.size >= 6 ? 12 : 6, md: c.size }}>
                                {renderCampo(c)}
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            ))}

            {errorGeneral && <Alert severity="error">{errorGeneral}</Alert>}

            <Box
                sx={{
                    display: "flex",
                    flexDirection: { xs: "column-reverse", sm: "row" },
                    justifyContent: "flex-end",
                    gap: 2,
                    mt: 1,
                }}
            >
                {bienId ? (
                    <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
                        Volver
                    </Button>
                ) : (
                    <Button variant="outlined" color="error" startIcon={<ClearIcon />} onClick={() => setForm(formularioVacio(tipo))}>
                        Limpiar formulario
                    </Button>
                )}
                <Button type="submit" variant="contained" color="primary" startIcon={<SaveIcon />} disabled={guardando} sx={{ px: 4 }}>
                    {guardando ? "Guardando…" : bienId ? "Guardar cambios" : `Registrar ${def.singular}`}
                </Button>
            </Box>

            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}
