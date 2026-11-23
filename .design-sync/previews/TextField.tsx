import { Box, InputAdornment, MenuItem, TextField } from "patrimonio-xochitepec";

const rejilla = { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2, p: 2, maxWidth: 640 };

export const Formulario = () => (
    <Box sx={rejilla}>
        <TextField required label="No. de control de inventario" defaultValue="0625-c" />
        <TextField required label="Descripción del artículo" defaultValue="MONITOR" />
        <TextField select label="Estado" defaultValue="BUENO">
            <MenuItem value="BUENO">BUENO</MenuItem>
            <MenuItem value="REGULAR">REGULAR</MenuItem>
            <MenuItem value="MALO">MALO</MenuItem>
        </TextField>
        <TextField
            label="Costo del bien"
            type="number"
            defaultValue="1500"
            slotProps={{ input: { startAdornment: <InputAdornment position="start">$</InputAdornment> } }}
        />
        <TextField label="Fecha de resguardo" type="date" defaultValue="2026-11-10" slotProps={{ inputLabel: { shrink: true } }} />
        <TextField label="Ubicación" placeholder="Edificio, piso, oficina" />
    </Box>
);

export const ConError = () => (
    <Box sx={rejilla}>
        <TextField required label="Correo electrónico" defaultValue="usuario@xochitepec" error helperText="Correo inválido" />
        <TextField label="Contraseña inicial" type="password" helperText="Mínimo 8 caracteres" />
    </Box>
);

export const Multilinea = () => (
    <Box sx={{ p: 2, maxWidth: 640 }}>
        <TextField
            fullWidth
            multiline
            minRows={3}
            label="Motivo del traspaso"
            defaultValue="Reasignación de equipo de cómputo a la Dirección de Planeación."
        />
    </Box>
);
