import { Box, Button } from "patrimonio-xochitepec";

const fila = { display: "flex", gap: 1.5, flexWrap: "wrap", alignItems: "center", p: 2 };

export const Variantes = () => (
    <Box sx={fila}>
        <Button variant="contained">Registrar bien mueble</Button>
        <Button variant="outlined">Revisar traspasos</Button>
        <Button>Ver todos</Button>
    </Box>
);

export const Colores = () => (
    <Box sx={fila}>
        <Button variant="contained" color="primary">Guardar</Button>
        <Button variant="contained" color="secondary">Patrimonio</Button>
        <Button variant="contained" color="info">Solicitar traspaso</Button>
        <Button variant="contained" color="success">Autorizar</Button>
        <Button variant="contained" color="error">Dar de baja</Button>
    </Box>
);

export const TamanosYEstados = () => (
    <Box sx={fila}>
        <Button variant="contained" size="small">Pequeño</Button>
        <Button variant="contained">Mediano</Button>
        <Button variant="contained" size="large">Grande</Button>
        <Button variant="contained" disabled>Guardando…</Button>
        <Button variant="outlined" color="error" disabled>Rechazar</Button>
    </Box>
);
