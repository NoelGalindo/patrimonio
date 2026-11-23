import { Box, EstatusTraspasoChip, Typography } from "patrimonio-xochitepec";

export const TodosLosEstatus = () => (
    <Box sx={{ display: "flex", gap: 1.5, flexWrap: "wrap", p: 2 }}>
        <EstatusTraspasoChip estatus="PENDIENTE" />
        <EstatusTraspasoChip estatus="AUTORIZADA" />
        <EstatusTraspasoChip estatus="RECHAZADA" />
        <EstatusTraspasoChip estatus="CANCELADA" />
    </Box>
);

export const EnUnaFila = () => (
    <Box sx={{ display: "flex", alignItems: "center", gap: 2, p: 2, flexWrap: "wrap" }}>
        <Typography variant="body2" sx={{ fontWeight: 700, color: "primary.main" }}>Folio 001/2026</Typography>
        <Typography variant="body2" color="text.secondary">Recursos Humanos → Planeación del Desarrollo Urbano</Typography>
        <EstatusTraspasoChip estatus="AUTORIZADA" />
    </Box>
);
