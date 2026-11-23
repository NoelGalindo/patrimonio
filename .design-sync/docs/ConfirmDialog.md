---
category: Xochitepec
---
Diálogo de confirmación para acciones importantes (autorizar, rechazar, dar de baja), con comentario opcional u obligatorio y manejo de error.

## Props

- `open` (bool, requerido): muestra el diálogo.
- `titulo` (string): título del diálogo.
- `mensaje` (string): explicación de la acción y sus consecuencias.
- `textoConfirmar` (string, "Confirmar"): texto del botón principal.
- `color` (`"primary" | "secondary" | "success" | "error" | "warning" | "info"`, "primary"): color del botón principal. Usa `error` para acciones destructivas y `success` para autorizaciones.
- `comentario` (`null | "opcional" | "obligatorio"`): agrega un campo de texto; si es obligatorio, el botón se deshabilita hasta escribir algo.
- `etiquetaComentario` (string, "Comentario"): etiqueta del campo.
- `onConfirm(comentario)` (requerido): puede devolver una promesa; mientras se resuelve el botón dice "Procesando…" y, si falla, el mensaje del error aparece en una alerta dentro del diálogo.
- `onClose` (requerido): cierra el diálogo.

Monta el diálogo con un `key` distinto en cada apertura para reiniciar el comentario.

## Ejemplo

```jsx
<ConfirmDialog
  open
  titulo="Rechazar traspaso"
  mensaje="Indica el motivo del rechazo; el área solicitante podrá verlo."
  textoConfirmar="Rechazar"
  color="error"
  comentario="obligatorio"
  etiquetaComentario="Motivo del rechazo"
  onConfirm={(motivo) => rechazar(motivo)}
  onClose={() => setAbierto(false)}
/>
```
