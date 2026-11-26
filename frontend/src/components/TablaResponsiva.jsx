import {
    Box,
    Card,
    CardActionArea,
    Checkbox,
    LinearProgress,
    Paper,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    Typography,
} from "@mui/material";
import useEsMovil from "../hooks/useEsMovil";

const vacio = (v) => v === null || v === undefined || v === "";

/**
 * Tabla que en escritorio se muestra como tabla y en celular como lista de tarjetas.
 *
 * columnas: [{ id, label, render(fila), align?, principal?, secundaria?, soloEscritorio? }]
 *   - principal: título de la tarjeta en celular (la primera columna si ninguna lo indica)
 *   - secundaria: subtítulo de la tarjeta en celular
 *   - soloEscritorio: no se muestra en la tarjeta
 * acciones(fila): botones de la fila (última columna / pie de la tarjeta)
 * seleccion: { esSeleccionada(fila), alternar(fila) } para mostrar casillas
 * paginacion: { count, page, rowsPerPage, rowsPerPageOptions?, onPageChange, onRowsPerPageChange? }
 */
export default function TablaResponsiva({
    columnas,
    filas,
    getKey = (f) => f.id,
    onClickFila,
    acciones,
    seleccion,
    cargando = false,
    textoVacio = "No hay registros.",
    paginacion,
    minWidth = 650,
    colorEncabezado = "rgba(0, 168, 135, 0.03)",
}) {
    const esMovil = useEsMovil();
    const principal = columnas.find((c) => c.principal) ?? columnas[0];
    const secundaria = columnas.find((c) => c.secundaria);
    const resto = columnas.filter((c) => c !== principal && c !== secundaria && !c.soloEscritorio);

    const pie = paginacion && (
        <TablePagination
            component="div"
            count={paginacion.count}
            page={paginacion.page}
            rowsPerPage={paginacion.rowsPerPage}
            rowsPerPageOptions={esMovil ? [] : paginacion.rowsPerPageOptions ?? []}
            onPageChange={paginacion.onPageChange}
            onRowsPerPageChange={paginacion.onRowsPerPageChange}
            labelRowsPerPage="Filas por página"
            labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
            sx={{ ".MuiTablePagination-toolbar": { flexWrap: "wrap", px: { xs: 1, sm: 2 } } }}
        />
    );

    const mensajeVacio = !cargando && filas.length === 0 && (
        <Typography color="text.secondary" align="center" sx={{ py: 6 }}>
            {textoVacio}
        </Typography>
    );

    if (esMovil) {
        return (
            <Box>
                {cargando && <LinearProgress sx={{ mb: 1, borderRadius: 1 }} />}
                <Stack spacing={1.5}>
                    {filas.map((fila) => {
                        const contenido = (
                            <Box sx={{ p: 2 }}>
                                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: "primary.main", wordBreak: "break-word" }}>
                                    {principal.render(fila)}
                                </Typography>
                                {secundaria && (
                                    <Typography variant="body2" sx={{ fontWeight: 600, wordBreak: "break-word" }}>
                                        {secundaria.render(fila)}
                                    </Typography>
                                )}
                                {resto.length > 0 && (
                                    <Box sx={{ display: "grid", gridTemplateColumns: "auto 1fr", columnGap: 1.5, rowGap: 0.5, mt: 1 }}>
                                        {resto.map((c) => {
                                            const valor = c.render(fila);
                                            return [
                                                <Typography key={`${c.id}-l`} variant="caption" color="text.secondary">
                                                    {c.label}
                                                </Typography>,
                                                <Typography key={`${c.id}-v`} variant="caption" sx={{ fontWeight: 600, wordBreak: "break-word" }} component="div">
                                                    {vacio(valor) ? "—" : valor}
                                                </Typography>,
                                            ];
                                        })}
                                    </Box>
                                )}
                            </Box>
                        );
                        return (
                            <Card key={getKey(fila)} variant="outlined" sx={{ borderRadius: 3 }}>
                                <Box sx={{ display: "flex", alignItems: "flex-start" }}>
                                    {seleccion && (
                                        <Checkbox
                                            sx={{ mt: 1, ml: 0.5 }}
                                            checked={seleccion.esSeleccionada(fila)}
                                            onChange={() => seleccion.alternar(fila)}
                                        />
                                    )}
                                    <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                                        {onClickFila ? (
                                            <CardActionArea onClick={() => onClickFila(fila)}>{contenido}</CardActionArea>
                                        ) : (
                                            contenido
                                        )}
                                    </Box>
                                </Box>
                                {acciones && (
                                    <Box sx={{ display: "flex", flexWrap: "wrap", justifyContent: "flex-end", gap: 0.5, px: 1, pb: 1 }}>
                                        {acciones(fila)}
                                    </Box>
                                )}
                            </Card>
                        );
                    })}
                </Stack>
                {mensajeVacio}
                {pie && (
                    <Paper variant="outlined" sx={{ mt: 1.5 }}>
                        {pie}
                    </Paper>
                )}
            </Box>
        );
    }

    return (
        <TableContainer component={Paper}>
            {cargando && <LinearProgress />}
            <Table sx={{ minWidth }}>
                <TableHead sx={{ bgcolor: colorEncabezado }}>
                    <TableRow>
                        {seleccion && <TableCell padding="checkbox" />}
                        {columnas.map((c) => (
                            <TableCell key={c.id} align={c.align} sx={{ fontWeight: 700 }}>
                                {c.label}
                            </TableCell>
                        ))}
                        {acciones && (
                            <TableCell align="right" sx={{ fontWeight: 700 }}>
                                Acciones
                            </TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {filas.map((fila) => (
                        <TableRow
                            key={getKey(fila)}
                            hover
                            sx={{ cursor: onClickFila ? "pointer" : "default" }}
                            onClick={onClickFila ? () => onClickFila(fila) : undefined}
                        >
                            {seleccion && (
                                <TableCell padding="checkbox" onClick={(e) => e.stopPropagation()}>
                                    <Checkbox checked={seleccion.esSeleccionada(fila)} onChange={() => seleccion.alternar(fila)} />
                                </TableCell>
                            )}
                            {columnas.map((c) => (
                                <TableCell
                                    key={c.id}
                                    align={c.align}
                                    sx={c === principal ? { fontWeight: 700, color: "primary.main" } : undefined}
                                >
                                    {c.render(fila)}
                                </TableCell>
                            ))}
                            {acciones && (
                                <TableCell align="right" onClick={(e) => e.stopPropagation()} sx={{ whiteSpace: "nowrap" }}>
                                    {acciones(fila)}
                                </TableCell>
                            )}
                        </TableRow>
                    ))}
                    {!cargando && filas.length === 0 && (
                        <TableRow>
                            <TableCell colSpan={columnas.length + 2} align="center" sx={{ py: 6 }}>
                                <Typography color="text.secondary">{textoVacio}</Typography>
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
            {pie}
        </TableContainer>
    );
}
