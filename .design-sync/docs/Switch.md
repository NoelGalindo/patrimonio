---
category: Controles MUI
---
Interruptor de MUI para activar o desactivar algo de inmediato (p. ej. un usuario activo/inactivo); el estado encendido es verde de marca.

## Uso en Patrimonio

- En tablas: `size="small"` sin etiqueta, dentro de la columna "Activo".
- En formularios: envuélvelo en `FormControlLabel` con etiqueta en español.
- `disabled` cuando la acción no aplica (no puedes desactivarte a ti mismo).

```jsx
<FormControlLabel control={<Switch checked={activo} onChange={alternar} />} label="Usuario activo" />
```
