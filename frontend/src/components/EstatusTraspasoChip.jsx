import { Chip } from "@mui/material";

const CONFIG = {
    PENDIENTE: { label: "Pendiente", color: "warning" },
    AUTORIZADA: { label: "Autorizada", color: "success" },
    RECHAZADA: { label: "Rechazada", color: "error" },
    CANCELADA: { label: "Cancelada", color: "default" },
};

export default function EstatusTraspasoChip({ estatus }) {
    const c = CONFIG[estatus] ?? { label: estatus, color: "default" };
    return <Chip label={c.label} color={c.color} size="small" sx={{ fontWeight: 700, borderRadius: 1.5 }} />;
}
