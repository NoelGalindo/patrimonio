# Notas de sincronización con Claude Design

Proyecto: "Patrimonio Xochitepec" — https://claude.ai/design/p/25e3195b-bdee-4a50-8011-39683e81b88c (forma `package`, sin Storybook).

## Cómo está armado

- El repo es una **app** (Vite + React 19 + MUI v9), no una librería: no hay `dist/` ni comando de build para el sistema de diseño. La entrada es `frontend/src/design-system/index.js` (`cfg.entry`), que solo reexporta los 5 componentes propios, `tema` (src/theme.js) y `export * from "@mui/material"` (unas 475 exportaciones en `window.PatrimonioXochitepec`).
- Los componentes son `.jsx` sin tipos, así que los `.d.ts` salen vacíos: **todos los contratos están escritos a mano en `cfg.dtsPropsFor`**.
- La documentación de cada componente está en `.design-sync/docs/<Nombre>.md` (`cfg.docsDir`). Su `category` define el grupo (`xochitepec` / `controles-mui`).
- Los 8 controles de MUI (Button, TextField, Chip, Card, Alert, Tabs, Switch, Dialog) apuntan en `componentSrcMap` a `src/theme.js` a propósito, por dos razones: (1) una ruta dentro de `node_modules/@mui/material/...` deriva el grupo "material" y le gana a la `category` de la documentación; (2) su aspecto lo define el tema, así que un cambio en `theme.js` vuelve a verificar sus tarjetas.
- Proveedor de las vistas: `ThemeProvider theme={tema}` + `CssBaseline` (`cfg.provider`, con `$ref: "tema"`).
- Fuentes: Outfit e Inter se cargan de Google Fonts mediante `@import` remoto en `frontend/src/design-system/fuentes.css` (`cfg.cssEntry`). La app las carga desde `frontend/index.html`.
- Tarjetas: `ConfirmDialog`, `Aviso` y `Dialog` usan `cardMode: "single"` (overlays); `TablaResponsiva` y `EncabezadoPagina` usan `"column"` (son más anchas que la celda de la cuadrícula).

## Entorno (Windows)

- Verificación visual con el Chrome instalado: `DS_CHROMIUM_PATH="C:/Program Files/Google/Chrome/Application/chrome.exe"`. Playwright se instaló en `.ds-sync/` con `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1` (no se descarga Chromium).
- `--node-modules frontend/node_modules`.
- Para reproducir: copiar los scripts a `.ds-sync/` e instalar en esa carpeta `esbuild ts-morph @types/react playwright`.

## Re-sincronizar

```sh
DS_CHROMIUM_PATH="C:/Program Files/Google/Chrome/Application/chrome.exe" \
node .ds-sync/resync.mjs --config .design-sync/config.json --node-modules frontend/node_modules \
  --out ./ds-bundle --remote .design-sync/.cache/remote-sync.json
```

(Antes, guardar en `.design-sync/.cache/remote-sync.json` el `_ds_sync.json` del proyecto.)

## Known render warns

- Ninguna en la primera sincronización (13/13 limpias, sin `[GRID_OVERFLOW]` después de aplicar `cardMode: "column"` al encabezado).

## Re-sync risks

- **`dtsPropsFor` y `docs/*.md` son copias manuales de la API.** Si cambian las props de `EncabezadoPagina`, `TablaResponsiva`, `ConfirmDialog`, `Aviso` o `EstatusTraspasoChip`, hay que actualizar ambos; el build no lo detecta.
- **Componentes nuevos en `frontend/src/components/`** no entran solos: hay que agregarlos al barrel `design-system/index.js` y a `componentSrcMap`. Los que llaman a la API (AreaSelect, SolicitarTraspasoDialog, DetalleBienDialog) se dejaron fuera a propósito.
- **Fuentes remotas:** dependen de que Google Fonts esté disponible; no se empaqueta ningún archivo de fuente.
- **El bundle incluye todo `@mui/material`** (~2 MB). Si se actualiza MUI, revisar que las vistas sigan bien (nuevo major ⇒ `--force`).
- Las convenciones (`conventions.md`) nombran exportaciones de MUI (`Grid`, `useMediaQuery`, `slotProps`…) que se verificaron contra el bundle de MUI v9.
