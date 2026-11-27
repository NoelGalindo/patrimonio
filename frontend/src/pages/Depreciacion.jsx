import { useState } from "react";
import { Alert, Box, Button, Card, CardContent, Grid, MenuItem, TextField, Typography } from "@mui/material";
import DownloadIcon from "@mui/icons-material/DownloadOutlined";
import useFetch from "../hooks/useFetch";
import EncabezadoPagina from "../components/EncabezadoPagina";
import TablaResponsiva from "../components/TablaResponsiva";
import { etiquetaRegla, etiquetaTipoContable } from "../bienes/depreciacion";
import { formatoMoneda } from "../utils/formato";

const ANIO_ACTUAL = new Date().getFullYear();
const ANIOS = Array.from({ length: 21 }, (_, i) => ANIO_ACTUAL + 5 - i);

/** Reporte anual de depreciación por cuenta contable (para Contabilidad). */
export default function Depreciacion() {
    const [anio, setAnio] = useState(ANIO_ACTUAL);
    const { data, error, cargando } = useFetch("/reportes/depreciacion", { anio });
    const filas = data?.cuentas.length ? [...data.cuentas, { ...data.total, clave: "", esTotal: true }] : [];
    const negrita = (f, texto) => (f.esTotal ? <strong>{texto}</strong> : texto);

    return (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
            <EncabezadoPagina
                titulo="Depreciación"
                descripcion="Depreciación del ejercicio y valor en libros al 31 de diciembre, por cuenta contable (Guía de Vida Útil del CONAC)."
            >
                <Button
                    variant="contained"
                    startIcon={<DownloadIcon />}
                    disabled={!data || data.cuentas.length === 0}
                    onClick={() => exportarCsv(data)}
                >
                    Exportar a Excel
                </Button>
            </EncabezadoPagina>

            <Card>
                <CardContent>
                    <Grid container spacing={2} alignItems="center">
                        <Grid size={{ xs: 12, sm: 4, md: 3 }}>
                            <TextField select fullWidth label="Ejercicio" value={anio} onChange={(e) => setAnio(Number(e.target.value))}>
                                {ANIOS.map((a) => (
                                    <MenuItem key={a} value={a}>{a}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 8, md: 9 }}>
                            {data && (
                                <Typography variant="body2" color="text.secondary">
                                    Regla vigente: <strong>{etiquetaRegla(data.regla)}</strong>. Valor de desecho: $1 por bien.
                                </Typography>
                            )}
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            {error && <Alert severity="error">{error.message}</Alert>}
            {data?.bienesSinCalculo > 0 && (
                <Alert severity="warning">
                    {data.bienesSinCalculo === 1 ? "1 bien no se incluye" : `${data.bienesSinCalculo} bienes no se incluyen`} porque le falta
                    cuenta contable, valor o fecha de compra. Revísalos en el inventario.
                </Alert>
            )}

            <TablaResponsiva
                cargando={cargando}
                minWidth={900}
                filas={filas}
                getKey={(f) => (f.esTotal ? "total" : f.clave)}
                textoVacio={`No hay bienes con depreciación en ${anio}.`}
                columnas={[
                    { id: "clave", label: "Cuenta", render: (f) => negrita(f, f.esTotal ? "Total" : f.clave), principal: true },
                    { id: "concepto", label: "Concepto", render: (f) => (f.esTotal ? "" : f.concepto), secundaria: true },
                    {
                        id: "tipo",
                        label: "Tipo / vida útil",
                        render: (f) => (f.esTotal ? "" : `${etiquetaTipoContable(f.tipoBien)} · ${f.vidaUtilAnios} años`),
                        soloEscritorio: true,
                    },
                    { id: "bienes", label: "Bienes", align: "right", render: (f) => negrita(f, f.bienes) },
                    { id: "valor", label: "Valor original", align: "right", render: (f) => negrita(f, formatoMoneda(f.valorOriginal)) },
                    {
                        id: "ejercicio",
                        label: `Depreciación ${anio}`,
                        align: "right",
                        render: (f) => negrita(f, formatoMoneda(f.depreciacionEjercicio)),
                    },
                    { id: "acumulada", label: "Acumulada al 31-dic", align: "right", render: (f) => negrita(f, formatoMoneda(f.acumulada)) },
                    { id: "libros", label: "Valor en libros", align: "right", render: (f) => negrita(f, formatoMoneda(f.valorLibros)) },
                ]}
            />
        </Box>
    );
}

/** Descarga el reporte como CSV (UTF-8 con BOM para que Excel respete los acentos). */
function exportarCsv(data) {
    const celda = (v) => `"${String(v ?? "").replace(/"/g, '""')}"`;
    const encabezado = [
        "Cuenta",
        "Concepto",
        "Tipo de bien",
        "Vida útil (años)",
        "Bienes",
        "Valor original",
        `Depreciación ${data.anio}`,
        `Acumulada al 31/12/${data.anio}`,
        "Valor en libros",
    ];
    const fila = (f, total) => [
        total ? "Total" : f.clave,
        total ? "" : f.concepto,
        total ? "" : etiquetaTipoContable(f.tipoBien),
        total ? "" : f.vidaUtilAnios,
        f.bienes,
        f.valorOriginal,
        f.depreciacionEjercicio,
        f.acumulada,
        f.valorLibros,
    ];
    const lineas = [
        [`Reporte de depreciación ${data.anio}`],
        [`Regla: ${etiquetaRegla(data.regla)}`],
        [],
        encabezado,
        ...data.cuentas.map((f) => fila(f, false)),
        fila(data.total, true),
    ];
    const csv = "﻿" + lineas.map((l) => l.map(celda).join(",")).join("\r\n");
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const a = document.createElement("a");
    a.href = url;
    a.download = `depreciacion-${data.anio}.csv`;
    a.click();
    URL.revokeObjectURL(url);
}
