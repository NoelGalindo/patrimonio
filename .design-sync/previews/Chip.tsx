import { Box, Chip } from "patrimonio-xochitepec";

const fila = { display: "flex", gap: 1.5, flexWrap: "wrap", p: 2 };
const negrita = { fontWeight: 700 };
const noop = () => {};

export const EstadosDeBien = () => (
    <Box sx={fila}>
        <Chip label="BUENO" color="success" size="small" sx={negrita} />
        <Chip label="REGULAR" color="warning" size="small" sx={negrita} />
        <Chip label="MALO" color="error" size="small" sx={negrita} />
        <Chip label="Dado de baja" color="error" size="small" variant="outlined" sx={negrita} />
    </Box>
);

export const Roles = () => (
    <Box sx={fila}>
        <Chip label="Administrador" color="primary" size="small" sx={negrita} />
        <Chip label="Patrimonio" color="secondary" size="small" sx={negrita} />
        <Chip label="Usuario de Área" color="info" size="small" sx={negrita} />
    </Box>
);

export const Filtros = () => (
    <Box sx={fila}>
        <Chip label="Área: Recursos Humanos" onDelete={noop} />
        <Chip label="Estado: BUENO" variant="outlined" onDelete={noop} />
    </Box>
);
