import { Box, Button, Card, CardActions, CardContent, Typography } from "patrimonio-xochitepec";

const contadores = [
    { titulo: "Bienes muebles", valor: "128", desc: "Activos en inventario", color: "info.main" },
    { titulo: "Traspasos pendientes", valor: "3", desc: "En espera de autorización", color: "warning.main" },
];

export const Contadores = () => (
    <Box sx={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 2, p: 2, maxWidth: 560 }}>
        {contadores.map((c) => (
            <Card key={c.titulo} sx={{ borderTop: "4px solid", borderTopColor: c.color }}>
                <CardContent>
                    <Typography variant="caption" sx={{ fontWeight: "bold", color: "text.secondary", textTransform: "uppercase", letterSpacing: 1 }}>
                        {c.titulo}
                    </Typography>
                    <Typography variant="h3" sx={{ fontWeight: 800, mt: 1, color: c.color }}>{c.valor}</Typography>
                    <Typography variant="caption" color="text.secondary">{c.desc}</Typography>
                </CardContent>
            </Card>
        ))}
    </Box>
);

export const SeccionDestacada = () => (
    <Box sx={{ p: 2, maxWidth: 640 }}>
        <Card sx={{ borderLeft: "6px solid", borderLeftColor: "secondary.main" }}>
            <CardContent>
                <Typography variant="h6" sx={{ fontWeight: "bold" }}>Traspasos por autorizar (3)</Typography>
                <Typography variant="body2" color="text.secondary">
                    Solicitudes de las áreas que requieren autorización de Patrimonio.
                </Typography>
            </CardContent>
            <CardActions sx={{ px: 2, pb: 2 }}>
                <Button variant="contained">Revisar traspasos</Button>
                <Button variant="outlined">Registrar alta</Button>
            </CardActions>
        </Card>
    </Box>
);
