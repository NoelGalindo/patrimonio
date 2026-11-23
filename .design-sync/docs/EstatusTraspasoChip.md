---
category: Xochitepec
---
Etiqueta de color con el estatus de una solicitud de traspaso: Pendiente, Autorizada, Rechazada o Cancelada.

## Props

- `estatus` (`"PENDIENTE" | "AUTORIZADA" | "RECHAZADA" | "CANCELADA"`, requerido): valor tal como lo devuelve la API.

| estatus | Texto | Color |
|---|---|---|
| `PENDIENTE` | Pendiente | warning (naranja) |
| `AUTORIZADA` | Autorizada | success (verde) |
| `RECHAZADA` | Rechazada | error (rojo) |
| `CANCELADA` | Cancelada | default (gris) |

## Ejemplo

```jsx
<EstatusTraspasoChip estatus="AUTORIZADA" />
```

Para otros estados de la app (activo/inactivo, roles) usa `Chip` de MUI con `size="small"` y el mismo estilo (`fontWeight: 700`).
