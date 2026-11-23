# Patrimonio Xochitepec — convenciones para diseñar

Sistema de gestión del patrimonio municipal del H. Ayuntamiento de Xochitepec. Toda la interfaz va **en español** (títulos, botones, mensajes, fechas `dd/mm/aaaa`, montos `$1,500.00`). Se usa mucho en **celular**: cada pantalla debe funcionar desde 360 px de ancho.

## 1. Envoltura obligatoria

Todo viene de `window.PatrimonioXochitepec`: los 5 componentes propios, el tema (`tema`) y **todo `@mui/material` v9** (Button, TextField, Grid, Box, Stack, Typography, Card, Dialog, Tabs, Chip, Alert, IconButton, Tooltip, InputAdornment, MenuItem, FormControlLabel, useMediaQuery, useTheme…). Sin `ThemeProvider` con `tema`, todo sale con el azul y la tipografía genéricos de MUI:

```jsx
const { ThemeProvider, CssBaseline, tema } = window.PatrimonioXochitepec;
<ThemeProvider theme={tema}>
  <CssBaseline />
  <App />
</ThemeProvider>
```

Las fuentes (Outfit, Inter) llegan por `styles.css`; no agregues otras.

## 2. Idioma de estilos: MUI + tema, sin clases CSS

No hay clases utilitarias ni CSS propio. Estiliza con props de MUI y `sx` usando **claves del tema**, nunca colores hex:

| Uso | Valor |
|---|---|
| Acción principal, verde turquesa #00A887 | `color="primary"` / `"primary.main"` |
| Acento fucsia #E40046 (Patrimonio, bajas) | `"secondary.main"` |
| Azul #009FE3 (traspasos) | `color="info"` / `"info.main"` |
| Estados | `success` (bueno, autorizada), `warning` (regular, pendiente), `error` (malo, rechazar, dar de baja) |
| Texto | `"text.primary"`, `"text.secondary"`; fondo de página `"background.default"` |
| Espaciado | unidades de 8 px: `gap: 3` entre bloques, `p: { xs: 2, sm: 3, md: 4 }` en la página |

El tema ya define: botones con esquinas de 8 px y sin mayúsculas, superficies (`Card`, `Paper`) de 12 px con sombra suave, `TextField` en tamaño `small` y `Tabs` desplazables. No los redefinas.

MUI v9: usa `<Grid size={{ xs: 12, md: 6 }}>` (no `item xs`) y `slotProps` (no `InputProps` ni `primaryTypographyProps`).

## 3. Patrones de pantalla

- Página = `Box` en columna con `gap: 3`: `EncabezadoPagina` → filtros dentro de un `Card` → `TablaResponsiva` → diálogos.
- Listados: siempre `TablaResponsiva` (tabla en escritorio, tarjetas en celular); nunca `Table` directo.
- Confirmaciones: `ConfirmDialog`. Notificaciones: `Aviso`. Estatus de traspasos: `EstatusTraspasoChip`.
- En celular, los diálogos se abren a pantalla completa: `fullScreen={useMediaQuery(tema.breakpoints.down("sm"))}`.
- Valores responsivos en `sx` (`{ xs, sm, md }`); nada de anchos fijos en px.

## 4. Dónde está la verdad

Antes de componer, lee `components/<grupo>/<Nombre>/<Nombre>.prompt.md` (uso y ejemplos) y `<Nombre>.d.ts` (props). Grupos: `xochitepec` (componentes propios) y `controles-mui` (cómo se ven los controles MUI con este tema).

## 5. Ejemplo

```jsx
const { Box, Button, Card, CardContent, TextField, Grid, EncabezadoPagina, TablaResponsiva, EstatusTraspasoChip } = window.PatrimonioXochitepec;

<Box sx={{ display: "flex", flexDirection: "column", gap: 3, p: { xs: 2, md: 4 } }}>
  <EncabezadoPagina titulo="Traspasos de Bienes" descripcion="Solicitudes de traspaso entre áreas.">
    <Button variant="contained" color="info">Solicitar traspaso</Button>
  </EncabezadoPagina>
  <Card>
    <CardContent>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 8 }}><TextField fullWidth placeholder="Buscar por folio o inventario…" /></Grid>
      </Grid>
    </CardContent>
  </Card>
  <TablaResponsiva
    columnas={[
      { id: "folio", label: "Folio", render: (t) => t.folio, principal: true },
      { id: "bienes", label: "Bienes", render: (t) => t.bienes, secundaria: true },
      { id: "estatus", label: "Estatus", render: (t) => <EstatusTraspasoChip estatus={t.estatus} /> },
    ]}
    filas={[{ id: 1, folio: "001/2026", bienes: "0625-c MONITOR", estatus: "AUTORIZADA" }]}
  />
</Box>
```
