import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from "patrimonio-xochitepec";

export const FormularioUsuario = () => (
    <Dialog open maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 800 }}>Nuevo usuario de área</DialogTitle>
        <DialogContent dividers>
            <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2 }}>
                <TextField required label="Nombre(s)" defaultValue="Diana" />
                <TextField required label="Apellido paterno" defaultValue="Martínez" />
                <TextField required label="Correo electrónico" defaultValue="rh@xochitepec.gob.mx" sx={{ gridColumn: "1 / -1" }} />
                <TextField
                    select
                    required
                    label="Área"
                    defaultValue="rh"
                    sx={{ gridColumn: "1 / -1" }}
                    helperText="Determina qué bienes podrá ver"
                >
                    <MenuItem value="rh">Recursos Humanos</MenuItem>
                    <MenuItem value="plan">Planeación del Desarrollo Urbano</MenuItem>
                </TextField>
            </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 1.5 }}>
            <Button>Cancelar</Button>
            <Button variant="contained">Guardar</Button>
        </DialogActions>
    </Dialog>
);
