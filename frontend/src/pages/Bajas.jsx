import { useState } from "react";
import { Alert, Box, Card, Tab, Tabs } from "@mui/material";
import useFetch from "../hooks/useFetch";
import { CLAVES_TIPO, TIPOS_BIEN, columnasDe } from "../bienes/tiposBien";
import valorCampo from "../bienes/valorCampo";
import DetalleBienDialog from "../components/DetalleBienDialog";
import EncabezadoPagina from "../components/EncabezadoPagina";
import TablaResponsiva from "../components/TablaResponsiva";
import { formatoFecha } from "../utils/formato";

/** Historial de bienes dados de baja (baja lógica). Para dar de baja se usa el Inventario. */
export default function Bajas() {
    const [tipo, setTipo] = useState("muebles");
    const [pagina, setPagina] = useState(0);
    const [detalle, setDetalle] = useState(null);
    const def = TIPOS_BIEN[tipo];
    const columnas = columnasDe(tipo);

    const { data, error, cargando } = useFetch(`/bienes/${tipo}`, {
        activo: false,
        page: pagina,
        size: 20,
        sort: "actualizadoEn,desc",
    });

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo="Bienes dados de baja"
                descripcion="Historial de bienes retirados del inventario activo. Para dar de baja un bien, búscalo en el Inventario."
            />

            <Card>
                <Tabs
                    value={tipo}
                    onChange={(_, t) => {
                        setTipo(t);
                        setPagina(0);
                    }}
                    sx={{ px: { xs: 0, sm: 2 } }}
                >
                    {CLAVES_TIPO.map((t) => (
                        <Tab key={t} value={t} label={TIPOS_BIEN[t].label} sx={{ fontWeight: 700 }} />
                    ))}
                </Tabs>
            </Card>

            {error && <Alert severity="error">{error.message}</Alert>}

            <TablaResponsiva
                columnas={[
                    ...columnas.map((c, i) => ({
                        id: c.name,
                        label: c.label,
                        align: c.type === "money" ? "right" : "left",
                        render: (b) => valorCampo(c, b),
                        principal: i === 0,
                        secundaria: c.name === def.titulo,
                    })),
                    { id: "area", label: "Área", render: (b) => b.area?.nombre },
                    { id: "baja", label: "Fecha de baja", render: (b) => formatoFecha(b.actualizadoEn) },
                ]}
                filas={data?.contenido ?? []}
                cargando={cargando}
                onClickFila={setDetalle}
                textoVacio="No hay bajas registradas."
                colorEncabezado="rgba(228, 0, 70, 0.03)"
                paginacion={{
                    count: data?.totalElementos ?? 0,
                    page: pagina,
                    rowsPerPage: 20,
                    onPageChange: (_, p) => setPagina(p),
                }}
            />

            <DetalleBienDialog tipo={tipo} bien={detalle} onClose={() => setDetalle(null)} />
        </Box>
    );
}
