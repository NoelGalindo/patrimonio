import { Alert, Box, Chip, Grid, Typography } from "@mui/material";
import useFetch from "../hooks/useFetch";
import { formatoFecha, formatoMoneda } from "../utils/formato";
import TablaResponsiva from "./TablaResponsiva";

function Dato({ label, children }) {
    return (
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
                {label}
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {children ?? "—"}
            </Typography>
        </Grid>
    );
}

/** Depreciación de un bien (Guía de Vida Útil del CONAC): resumen a hoy y calendario por año. */
export default function DepreciacionBien({ tipo, bien }) {
    const d = bien.depreciacion;
    const cuenta = bien.cuentaContable;
    const calculable = d && !d.motivo;
    const { data: calendario, cargando } = useFetch(calculable ? `/bienes/${tipo}/${bien.id}/depreciacion` : null);

    return (
        <Box>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, flexWrap: "wrap", mb: 1.5 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: "primary.main", flexGrow: 1 }}>
                    Depreciación
                </Typography>
                {calculable &&
                    (d.totalmenteDepreciado ? (
                        <Chip size="small" color="warning" label="Totalmente depreciado · en uso con valor de $1" sx={{ fontWeight: 700 }} />
                    ) : (
                        <Chip size="small" color="success" label="En depreciación" sx={{ fontWeight: 700 }} />
                    ))}
            </Box>

            <Grid container spacing={1.5}>
                <Dato label="Cuenta contable">{cuenta ? `${cuenta.clave} — ${cuenta.concepto}` : null}</Dato>
                <Dato label="Vida útil">
                    {cuenta?.vidaUtilAnios ? `${cuenta.vidaUtilAnios} años (${cuenta.porcentajeAnual} % anual)` : null}
                </Dato>
                {calculable && (
                    <>
                        <Dato label="Valor a depreciar">{formatoMoneda(d.valorOriginal)}</Dato>
                        <Dato label="Depreciación anual">{formatoMoneda(d.depreciacionAnual)}</Dato>
                        <Dato label="Periodo de depreciación">
                            {formatoFecha(d.fechaInicio)} – {d.fechaFin ? formatoFecha(d.fechaFin) : "—"}
                        </Dato>
                        <Dato label="Depreciación del ejercicio">{formatoMoneda(d.depreciacionEjercicio)}</Dato>
                        <Dato label="Depreciación acumulada a hoy">{formatoMoneda(d.acumulada)}</Dato>
                        <Dato label="Valor en libros a hoy">{formatoMoneda(d.valorLibros)}</Dato>
                    </>
                )}
            </Grid>

            {d?.motivo && (
                <Alert severity="info" sx={{ mt: 1.5 }}>
                    No se puede calcular la depreciación: {d.motivo.charAt(0).toLowerCase() + d.motivo.slice(1)}.
                </Alert>
            )}

            {calculable && (
                <Box sx={{ mt: 2 }}>
                    <TablaResponsiva
                        cargando={cargando}
                        minWidth={420}
                        textoVacio="Sin periodos de depreciación"
                        getKey={(p) => p.anio}
                        filas={calendario ?? []}
                        columnas={[
                            { id: "anio", label: "Año", render: (p) => p.anio, principal: true },
                            { id: "dep", label: "Depreciación", align: "right", render: (p) => formatoMoneda(p.depreciacion) },
                            { id: "acum", label: "Acumulada", align: "right", render: (p) => formatoMoneda(p.acumulada) },
                            {
                                id: "libros",
                                label: "Valor en libros al 31-dic",
                                align: "right",
                                render: (p) => formatoMoneda(p.valorLibros),
                                secundaria: true,
                            },
                        ]}
                    />
                </Box>
            )}
        </Box>
    );
}
