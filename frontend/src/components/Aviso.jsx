import { Alert, Snackbar } from "@mui/material";

/** Notificación flotante. `aviso` = { mensaje, tipo: "success" | "error" | "info" } o null. */
export default function Aviso({ aviso, onClose }) {
    return (
        <Snackbar
            open={Boolean(aviso)}
            autoHideDuration={6000}
            onClose={onClose}
            anchorOrigin={{ vertical: "top", horizontal: "right" }}
        >
            <Alert severity={aviso?.tipo ?? "info"} onClose={onClose} variant="filled" sx={{ borderRadius: 2 }}>
                {aviso?.mensaje}
            </Alert>
        </Snackbar>
    );
}
