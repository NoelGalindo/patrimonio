// Punto de entrada del sistema de diseño de Patrimonio Xochitepec.
// Lo usa la sincronización con Claude Design (.design-sync/config.json) para empaquetar
// los componentes reales de la app; la app no lo importa. Solo reexporta: no agregar
// lógica aquí.

// Componentes visuales propios (no dependen de la API)
export { default as EncabezadoPagina } from "../components/EncabezadoPagina";
export { default as TablaResponsiva } from "../components/TablaResponsiva";
export { default as EstatusTraspasoChip } from "../components/EstatusTraspasoChip";
export { default as ConfirmDialog } from "../components/ConfirmDialog";
export { default as Aviso } from "../components/Aviso";

// Tema de marca (colores, tipografía y ajustes de componentes de MUI)
export { default as tema } from "../theme";

// Componentes base de MUI (Button, TextField, Card, ThemeProvider, CssBaseline...),
// para construir pantallas con el tema de Xochitepec
export * from "@mui/material";
