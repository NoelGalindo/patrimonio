---
category: Controles MUI
---
Tarjeta de MUI (superficie `Paper`): en este tema tiene esquinas de 12 px y una sombra azul grisácea suave; es el contenedor de cada bloque de una pantalla.

## Uso en Patrimonio

- Compón con `CardContent` (y `CardActions` para botones al pie).
- Tarjeta de contador del Inicio: borde superior de color (`borderTop: "4px solid"`, `borderTopColor: "info.main"`), etiqueta `caption` en mayúsculas y el número en `h3` con `fontWeight: 800`.
- Sección destacada por rol: borde izquierdo de 6 px (`borderLeft: "6px solid"`, `borderLeftColor: "secondary.main"`).
- `variant="outlined"` para tarjetas dentro de otra tarjeta.

```jsx
<Card>
  <CardContent>
    <Typography variant="h6" sx={{ fontWeight: "bold" }}>Últimos traspasos</Typography>
  </CardContent>
</Card>
```
