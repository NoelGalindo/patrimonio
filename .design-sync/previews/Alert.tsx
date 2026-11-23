import { Alert, Box } from "patrimonio-xochitepec";

const columna = { display: "flex", flexDirection: "column", gap: 1.5, p: 2, maxWidth: 640 };

export const Severidades = () => (
    <Box sx={columna}>
        <Alert severity="error">No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.</Alert>
        <Alert severity="warning">Tu usuario no tiene un área asignada. Solicita al administrador que te asigne una.</Alert>
        <Alert severity="info">Para dar de baja un bien, búscalo en el Inventario.</Alert>
        <Alert severity="success">Traspaso autorizado con folio 001/2026.</Alert>
    </Box>
);

export const PendientesOutlined = () => (
    <Box sx={columna}>
        <Alert severity="warning" variant="outlined" sx={{ borderRadius: 2 }}>
            <strong>0626-c, 0627-c</strong> — Recursos Humanos → Planeación del Desarrollo Urbano
        </Alert>
        <Alert severity="warning" variant="outlined" sx={{ borderRadius: 2 }}>
            <strong>0702-a</strong> — Tesorería → Obras Públicas
        </Alert>
    </Box>
);
