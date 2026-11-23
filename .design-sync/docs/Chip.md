---
category: Controles MUI
---
Etiqueta compacta de MUI para estados, roles y conteos; en Patrimonio se usa `size="small"` con `fontWeight: 700`.

## Uso en Patrimonio

- Estados de un bien: `success` (BUENO), `warning` (REGULAR), `error` (MALO o "Dado de baja").
- Roles de usuario: `primary` (Administrador), `secondary` (Patrimonio), `info` (Usuario de Área).
- Para el estatus de traspasos usa `EstatusTraspasoChip`, no un `Chip` suelto.
- `variant="outlined"` para etiquetas informativas; `onDelete` para filtros que se pueden quitar.

```jsx
<Chip label="BUENO" color="success" size="small" sx={{ fontWeight: 700 }} />
```
