---
category: Controles MUI
---
Botón de MUI con el tema de Xochitepec: esquinas de 8 px, texto sin mayúsculas en peso 600 y sombra verde suave al pasar el cursor.

## Uso en Patrimonio

- `variant="contained"` (color `primary`, verde #00A887): la acción principal de la pantalla o del diálogo. Una por bloque.
- `variant="outlined"`: acciones secundarias (Revisar, Solicitar traspaso).
- `variant="text"`: Cancelar y enlaces de acción ("Ver todos").
- `color`: `primary` (verde turquesa), `secondary` (fucsia #E40046), `info` (azul #009FE3, traspasos), `success` (autorizar), `error` (dar de baja, rechazar), `warning`.
- `startIcon` / `endIcon` para íconos; `size="small"` en filas de tabla; `fullWidth` en formularios de celular.
- `disabled` mientras se envía y cambia el texto a "Guardando…".

```jsx
<Button variant="contained" startIcon={<SaveIcon />}>Registrar bien mueble</Button>
<Button variant="outlined" color="info">Solicitar traspaso</Button>
<Button color="error">Rechazar</Button>
```
