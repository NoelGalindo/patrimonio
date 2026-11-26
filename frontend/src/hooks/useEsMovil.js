import { useMediaQuery, useTheme } from "@mui/material";

/** true en pantallas de celular (menos de 600 px de ancho). */
export default function useEsMovil() {
    const theme = useTheme();
    return useMediaQuery(theme.breakpoints.down("sm"));
}
