import { useState } from "react";
import {
    Alert,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    TextField,
} from "@mui/material";

/**
 * Diálogo de confirmación con comentario opcional u obligatorio.
 * `onConfirm(comentario)` puede devolver una promesa; si falla se muestra el error.
 * Se debe montar con `key` distinta por cada apertura para reiniciar su estado.
 */
export default function ConfirmDialog({
    open,
    titulo,
    mensaje,
    textoConfirmar = "Confirmar",
    color = "primary",
    comentario = null, // null | "opcional" | "obligatorio"
    etiquetaComentario = "Comentario",
    onConfirm,
    onClose,
}) {
    const [texto, setTexto] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState("");

    const faltaComentario = comentario === "obligatorio" && texto.trim() === "";

    const confirmar = async () => {
        setEnviando(true);
        setError("");
        try {
            await onConfirm(texto.trim());
            onClose();
        } catch (e) {
            setError(e.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <Dialog open={open} onClose={enviando ? undefined : onClose} maxWidth="xs" fullWidth>
            <DialogTitle sx={{ fontWeight: 700 }}>{titulo}</DialogTitle>
            <DialogContent>
                <DialogContentText sx={{ mb: comentario ? 2 : 0 }}>{mensaje}</DialogContentText>
                {comentario && (
                    <TextField
                        fullWidth
                        multiline
                        minRows={2}
                        label={etiquetaComentario}
                        required={comentario === "obligatorio"}
                        value={texto}
                        onChange={(e) => setTexto(e.target.value)}
                        autoFocus
                    />
                )}
                {error && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        {error}
                    </Alert>
                )}
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2 }}>
                <Button onClick={onClose} disabled={enviando}>
                    Cancelar
                </Button>
                <Button variant="contained" color={color} onClick={confirmar} disabled={enviando || faltaComentario}>
                    {enviando ? "Procesando…" : textoConfirmar}
                </Button>
            </DialogActions>
        </Dialog>
    );
}
