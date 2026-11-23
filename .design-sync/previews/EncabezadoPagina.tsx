import { Box, Button, EncabezadoPagina } from "patrimonio-xochitepec";

export const ConAcciones = () => (
    <Box sx={{ p: 2 }}>
        <EncabezadoPagina
            titulo="Inventario de Bienes"
            descripcion="Relación de bienes muebles, inmuebles y vehículos de todas las áreas."
        >
            <Button variant="outlined" color="info">Solicitar traspaso</Button>
            <Button variant="contained">Registrar bien mueble</Button>
        </EncabezadoPagina>
    </Box>
);

export const SoloTitulo = () => (
    <Box sx={{ p: 2 }}>
        <EncabezadoPagina
            titulo="Bienes dados de baja"
            descripcion="Historial de bienes retirados del inventario activo. Para dar de baja un bien, búscalo en el Inventario."
        />
    </Box>
);

export const UnaAccion = () => (
    <Box sx={{ p: 2 }}>
        <EncabezadoPagina
            titulo="Usuarios de Área"
            descripcion="Alta y mantenimiento de los usuarios de cada área. Solo verán los bienes del área que les asignes."
        >
            <Button variant="contained">Nuevo usuario</Button>
        </EncabezadoPagina>
    </Box>
);
