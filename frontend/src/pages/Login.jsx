import {
    Box,
    Button,
    Paper,
    TextField,
    Typography
} from "@mui/material";

export default function Login() {

    return (

        <Paper
            elevation={5}
            sx={{
                width: 420,
                p: 5,
                borderRadius: 3
            }}
        >

            <Typography
                variant="h4"
                fontWeight="bold"
                mb={1}
            >
                Bienvenido
            </Typography>

            <Typography
                color="text.secondary"
                mb={4}
            >
                Inicia sesión para continuar.
            </Typography>

            <TextField
                fullWidth
                label="Usuario"
                margin="normal"
            />

            <TextField
                fullWidth
                label="Contraseña"
                type="password"
                margin="normal"
            />

            <Button
                variant="contained"
                fullWidth
                size="large"
                sx={{
                    mt: 3,
                    py: 1.5
                }}
            >
                Iniciar sesión
            </Button>

        </Paper>

    );

}