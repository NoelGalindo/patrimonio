---
category: Xochitepec
---
Lista de registros que se muestra como tabla en escritorio y como tarjetas apiladas en celular (<600 px), con acciones, selección y paginación.

Úsala para cualquier listado (bienes, usuarios, traspasos). No uses `Table` de MUI directamente: en celular se desborda.

## Props

- `columnas` (requerido): arreglo de `{ id, label, render(fila), align?, principal?, secundaria?, soloEscritorio? }`.
  - `render` devuelve el contenido de la celda (texto o JSX, p. ej. un `Chip`).
  - `principal`: título de la tarjeta en celular (por defecto la primera columna). En escritorio se pinta en verde y negritas.
  - `secundaria`: subtítulo de la tarjeta en celular.
  - `soloEscritorio`: la columna no aparece en la tarjeta de celular.
- `filas` (requerido): arreglo de objetos.
- `getKey(fila)`: llave única; por defecto `fila.id`.
- `onClickFila(fila)`: hace clicable cada fila o tarjeta (p. ej. para abrir el detalle).
- `acciones(fila)`: botones por fila (`IconButton` con `Tooltip`); van en la última columna o al pie de la tarjeta.
- `seleccion`: `{ esSeleccionada(fila), alternar(fila) }` para mostrar casillas.
- `cargando` (bool): barra de progreso arriba.
- `textoVacio` (string): mensaje cuando no hay filas.
- `paginacion`: `{ count, page, rowsPerPage, rowsPerPageOptions?, onPageChange, onRowsPerPageChange? }` (paginación del servidor, textos en español).
- `minWidth` (número, 650): ancho mínimo de la tabla de escritorio.
- `colorEncabezado` (color CSS): fondo del encabezado de la tabla.

## Ejemplo

```jsx
<TablaResponsiva
  columnas={[
    { id: "inv", label: "No. de inventario", render: (b) => b.inventario, principal: true },
    { id: "desc", label: "Descripción", render: (b) => b.descripcion, secundaria: true },
    { id: "area", label: "Área", render: (b) => b.area },
    { id: "costo", label: "Costo", align: "right", render: (b) => b.costo },
  ]}
  filas={bienes}
  onClickFila={(b) => abrirDetalle(b)}
  paginacion={{ count: 42, page: 0, rowsPerPage: 20, onPageChange: (_, p) => setPagina(p) }}
/>
```
