---
category: Controles MUI
---
Mensaje en línea de MUI para errores, advertencias y confirmaciones dentro de la página o de un diálogo.

## Uso en Patrimonio

- `severity="error"`: el mensaje que devuelve la API (en español) al fallar una operación.
- `severity="warning"`: situaciones que requieren acción ("Tu usuario no tiene un área asignada").
- `severity="info"` / `"success"`: avisos permanentes; para confirmaciones temporales usa `Aviso`.
- `variant="outlined"` para listas de pendientes (cada traspaso por autorizar es un `Alert` outlined `warning`).

```jsx
<Alert severity="error">No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.</Alert>
```
