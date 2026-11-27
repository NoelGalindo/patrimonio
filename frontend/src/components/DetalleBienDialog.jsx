import {
    Box,
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Grid,
    Typography,
} from "@mui/material";
import { TIPOS_BIEN } from "../bienes/tiposBien";
import valorCampo from "../bienes/valorCampo";
import useEsMovil from "../hooks/useEsMovil";
import DepreciacionBien from "./DepreciacionBien";

/** Ficha completa de un bien, con todos los campos de su tipo. */
export default function DetalleBienDialog({ tipo, bien, onClose, acciones }) {
    const esMovil = useEsMovil();
    if (!bien) return null;
    const def = TIPOS_BIEN[tipo];

    return (
        <Dialog open onClose={onClose} maxWidth="md" fullWidth fullScreen={esMovil}>
            <DialogTitle sx={{ display: "flex", alignItems: "center", gap: 1.5, flexWrap: "wrap" }}>
                <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="caption" color="text.secondary" sx={{ textTransform: "uppercase" }}>
                        {def.singular} · {bien[def.clave]}
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 800 }}>
                        {bien[def.titulo]}
                    </Typography>
                </Box>
                {!bien.activo && <Chip label="Dado de baja" color="error" size="small" />}
            </DialogTitle>
            <DialogContent dividers>
                <Typography variant="body2" sx={{ mb: 2 }}>
                    <strong>Área:</strong> {bien.area?.nombre}{" "}
                    <Typography component="span" variant="caption" color="text.secondary">
                        ({bien.area?.direccionGeneral})
                    </Typography>
                </Typography>
                {def.secciones.map((s, i) => (
                    <Box key={s.titulo} sx={{ mb: 2 }}>
                        {i > 0 && <Divider sx={{ mb: 2 }} />}
                        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: "primary.main", mb: 1.5 }}>
                            {s.titulo}
                        </Typography>
                        <Grid container spacing={1.5}>
                            {s.campos.map((c) => (
                                <Grid key={c.name} size={{ xs: 12, sm: c.type === "textarea" ? 12 : 6, md: c.type === "textarea" ? 12 : 4 }}>
                                    <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>
                                        {c.label}
                                    </Typography>
                                    <Typography variant="body2" sx={{ fontWeight: 600, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                                        {valorCampo(c, bien) || "—"}
                                    </Typography>
                                </Grid>
                            ))}
                        </Grid>
                    </Box>
                ))}
                <Divider sx={{ mb: 2 }} />
                <DepreciacionBien tipo={tipo} bien={bien} />
            </DialogContent>
            <DialogActions sx={{ px: 3, py: 1.5 }}>
                {acciones}
                <Button onClick={onClose}>Cerrar</Button>
            </DialogActions>
        </Dialog>
    );
}
