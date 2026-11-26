import { Box, Typography } from "@mui/material";

/**
 * Título y descripción de página con sus botones de acción.
 * En celular los botones pasan debajo del título y ocupan todo el ancho.
 */
export default function EncabezadoPagina({ titulo, descripcion, children }) {
    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: { xs: "column", sm: "row" },
                justifyContent: "space-between",
                alignItems: { xs: "stretch", sm: "center" },
                gap: 2,
            }}
        >
            <Box sx={{ minWidth: 0 }}>
                <Typography variant="h5" sx={{ fontWeight: 800, color: "text.primary" }}>
                    {titulo}
                </Typography>
                {descripcion && (
                    <Typography variant="body2" color="text.secondary">
                        {descripcion}
                    </Typography>
                )}
            </Box>
            {children && (
                <Box
                    sx={{
                        display: "flex",
                        flexDirection: { xs: "column", sm: "row" },
                        gap: 1,
                        flexShrink: 0,
                        "& > *": { width: { xs: "100%", sm: "auto" } },
                    }}
                >
                    {children}
                </Box>
            )}
        </Box>
    );
}
