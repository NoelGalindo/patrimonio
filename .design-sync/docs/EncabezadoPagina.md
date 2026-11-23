---
category: Xochitepec
---
Encabezado estándar de página: título, descripción y botones de acción; en celular los botones bajan y ocupan todo el ancho.

Úsalo como primer elemento de cada pantalla, dentro del contenedor de la página (un `Box` en columna con `gap: 3`).

## Props

- `titulo` (string, requerido): título de la página. Se muestra como `h5` en negritas.
- `descripcion` (string, opcional): una o dos líneas que explican para qué sirve la pantalla.
- `children` (opcional): botones de acción (`Button` de MUI). En pantallas ≥600 px van a la derecha del título; en celular se apilan debajo con ancho completo.

## Ejemplo

```jsx
<EncabezadoPagina
  titulo="Inventario de Bienes"
  descripcion="Relación de bienes muebles, inmuebles y vehículos de todas las áreas."
>
  <Button variant="outlined" color="info">Solicitar traspaso</Button>
  <Button variant="contained">Registrar bien mueble</Button>
</EncabezadoPagina>
```

Convención: el botón principal (`variant="contained"`) va al final; las acciones secundarias usan `variant="outlined"`.
