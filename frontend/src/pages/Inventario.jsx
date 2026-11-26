import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    IconButton,
    InputAdornment,
    Tab,
    Tabs,
    TextField,
    Tooltip,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/EditOutlined";
import DeleteIcon from "@mui/icons-material/DeleteOutlined";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import useAuth from "../auth/useAuth";
import { ROLES } from "../auth/roles";
import useFetch from "../hooks/useFetch";
import useDebounce from "../hooks/useDebounce";
import { bienesApi } from "../api/endpoints";
import { CLAVES_TIPO, TIPOS_BIEN, columnasDe } from "../bienes/tiposBien";
import valorCampo from "../bienes/valorCampo";
import AreaSelect from "../components/AreaSelect";
import Aviso from "../components/Aviso";
import ConfirmDialog from "../components/ConfirmDialog";
import DetalleBienDialog from "../components/DetalleBienDialog";
import SolicitarTraspasoDialog from "../components/SolicitarTraspasoDialog";
import TablaResponsiva from "../components/TablaResponsiva";
import EncabezadoPagina from "../components/EncabezadoPagina";

export default function Inventario() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const { usuario } = useAuth();
    const esPatrimonio = usuario.rol === ROLES.PATRIMONIO;
    const esUsuario = usuario.rol === ROLES.USUARIO;

    const tipoParam = searchParams.get("tipo");
    const tipo = CLAVES_TIPO.includes(tipoParam) ? tipoParam : "muebles";
    const def = TIPOS_BIEN[tipo];
    const columnas = columnasDe(tipo);

    const [busqueda, setBusqueda] = useState("");
    const [areaId, setAreaId] = useState("");
    const [pagina, setPagina] = useState(0);
    const [tamanio, setTamanio] = useState(20);
    const [seleccion, setSeleccion] = useState([]);
    const [detalle, setDetalle] = useState(null);
    const [porDarDeBaja, setPorDarDeBaja] = useState(null);
    const [traspaso, setTraspaso] = useState(null); // { key, bienes }
    const [aviso, setAviso] = useState(null);

    const q = useDebounce(busqueda);
    const { data, error, cargando, recargar } = useFetch(`/bienes/${tipo}`, {
        q,
        areaId: esUsuario ? "" : areaId,
        page: pagina,
        size: tamanio,
    });
    const filas = data?.contenido ?? [];

    // Solo un USUARIO o Patrimonio solicitan traspasos, y solo de bienes muebles
    const puedeSolicitar = tipo === "muebles" && (esUsuario || esPatrimonio);

    const cambiarTipo = (_, nuevo) => {
        setSearchParams({ tipo: nuevo });
        setPagina(0);
        setSeleccion([]);
    };

    const alternarSeleccion = (bien) => {
        if (seleccion.some((b) => b.id === bien.id)) {
            setSeleccion(seleccion.filter((b) => b.id !== bien.id));
        } else if (seleccion.length > 0 && seleccion[0].area.id !== bien.area.id) {
            // Todos los bienes de una solicitud deben ser de la misma área
            setAviso({ tipo: "info", mensaje: "Solo puedes agrupar bienes de la misma área en una solicitud." });
        } else {
            setSeleccion([...seleccion, bien]);
        }
    };

    const darDeBaja = async () => {
        await bienesApi.darDeBaja(tipo, porDarDeBaja.id);
        setAviso({ tipo: "success", mensaje: `Se dio de baja el ${def.singular} ${porDarDeBaja[def.clave]}.` });
        setDetalle(null);
        recargar();
    };

    const accionesFila = (bien) => (
        <>
            <Tooltip title="Editar">
                <IconButton size="small" onClick={() => navigate(`/dashboard/altas?tipo=${tipo}&id=${bien.id}`)}>
                    <EditIcon fontSize="small" />
                </IconButton>
            </Tooltip>
            <Tooltip title="Dar de baja">
                <IconButton size="small" color="error" onClick={() => setPorDarDeBaja(bien)}>
                    <DeleteIcon fontSize="small" />
                </IconButton>
            </Tooltip>
        </>
    );

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo="Inventario de Bienes"
                descripcion={
                    esUsuario
                        ? `Bienes asignados a tu área: ${usuario.area?.nombre ?? "sin área asignada"}.`
                        : "Relación de bienes muebles, inmuebles y vehículos de todas las áreas."
                }
            >
                {puedeSolicitar && (
                    <Button
                        variant="outlined"
                        color="info"
                        startIcon={<SwapHorizIcon />}
                        onClick={() => setTraspaso({ key: Date.now(), bienes: seleccion })}
                    >
                        Solicitar traspaso{seleccion.length > 0 ? ` (${seleccion.length})` : ""}
                    </Button>
                )}
                {esPatrimonio && (
                    <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate(`/dashboard/altas?tipo=${tipo}`)}>
                        Registrar {def.singular}
                    </Button>
                )}
            </EncabezadoPagina>

            <Card>
                <Tabs value={tipo} onChange={cambiarTipo} sx={{ px: { xs: 0, sm: 2 }, borderBottom: 1, borderColor: "divider" }}>
                    {CLAVES_TIPO.map((t) => (
                        <Tab key={t} value={t} label={TIPOS_BIEN[t].label} sx={{ fontWeight: 700 }} />
                    ))}
                </Tabs>
                <CardContent sx={{ py: 2, "&:last-child": { pb: 2 } }}>
                    <Grid container spacing={2} alignItems="center">
                        <Grid size={{ xs: 12, md: esUsuario ? 12 : 7 }}>
                            <TextField
                                fullWidth
                                placeholder="Buscar por número de inventario, descripción, marca, serie…"
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
                        {!esUsuario && (
                            <Grid size={{ xs: 12, md: 5 }}>
                                <AreaSelect
                                    label="Filtrar por área"
                                    value={areaId}
                                    onChange={(id) => {
                                        setAreaId(id);
                                        setPagina(0);
                                    }}
                                />
                            </Grid>
                        )}
                    </Grid>
                </CardContent>
            </Card>

            {error && <Alert severity="error">{error.message}</Alert>}

            <TablaResponsiva
                columnas={[
                    ...columnas.map((c, i) => ({
                        id: c.name,
                        label: c.label,
                        align: c.type === "money" ? "right" : "left",
                        render: (bien) => valorCampo(c, bien),
                        principal: i === 0,
                        secundaria: c.name === def.titulo,
                    })),
                    ...(esUsuario ? [] : [{ id: "area", label: "Área", render: (bien) => bien.area?.nombre }]),
                ]}
                filas={filas}
                cargando={cargando}
                onClickFila={setDetalle}
                acciones={esPatrimonio ? accionesFila : undefined}
                seleccion={
                    puedeSolicitar
                        ? { esSeleccionada: (bien) => seleccion.some((b) => b.id === bien.id), alternar: alternarSeleccion }
                        : undefined
                }
                textoVacio={`No se encontraron ${def.label.toLowerCase()}.`}
                paginacion={{
                    count: data?.totalElementos ?? 0,
                    page: pagina,
                    rowsPerPage: tamanio,
                    rowsPerPageOptions: [10, 20, 50, 100],
                    onPageChange: (_, p) => setPagina(p),
                    onRowsPerPageChange: (e) => {
                        setTamanio(Number(e.target.value));
                        setPagina(0);
                    },
                }}
            />

            <DetalleBienDialog
                tipo={tipo}
                bien={detalle}
                onClose={() => setDetalle(null)}
                acciones={
                    detalle && (
                        <>
                            {puedeSolicitar && (
                                <Button
                                    color="info"
                                    startIcon={<SwapHorizIcon />}
                                    onClick={() => {
                                        setTraspaso({ key: Date.now(), bienes: [detalle] });
                                        setDetalle(null);
                                    }}
                                >
                                    Solicitar traspaso
                                </Button>
                            )}
                            {esPatrimonio && accionesFila(detalle)}
                        </>
                    )
                }
            />

            {porDarDeBaja && (
                <ConfirmDialog
                    key={porDarDeBaja.id}
                    open
                    titulo={`Dar de baja ${def.singular}`}
                    mensaje={`¿Confirmas la baja de ${porDarDeBaja[def.clave]} — ${porDarDeBaja[def.titulo]}? El registro se conserva en el historial de bajas.`}
                    textoConfirmar="Dar de baja"
                    color="error"
                    onConfirm={darDeBaja}
                    onClose={() => setPorDarDeBaja(null)}
                />
            )}

            {traspaso && (
                <SolicitarTraspasoDialog
                    key={traspaso.key}
                    open
                    bienesIniciales={traspaso.bienes}
                    onClose={() => setTraspaso(null)}
                    onCreada={() => {
                        setSeleccion([]);
                        setAviso({ tipo: "success", mensaje: "Solicitud enviada a Patrimonio para su autorización." });
                    }}
                />
            )}

            <Aviso aviso={aviso} onClose={() => setAviso(null)} />
        </Box>
    );
}
