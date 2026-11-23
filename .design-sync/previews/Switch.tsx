import { Box, FormControlLabel, Switch } from "patrimonio-xochitepec";

export const Estados = () => (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 1, p: 2 }}>
        <FormControlLabel control={<Switch defaultChecked />} label="Usuario activo" />
        <FormControlLabel control={<Switch />} label="Usuario inactivo" />
        <FormControlLabel control={<Switch defaultChecked disabled />} label="Tu propio usuario (no se puede desactivar)" />
    </Box>
);

export const EnTabla = () => (
    <Box sx={{ display: "flex", gap: 2, alignItems: "center", p: 2 }}>
        <Switch size="small" defaultChecked />
        <Switch size="small" />
    </Box>
);
