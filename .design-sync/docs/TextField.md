---
category: Controles MUI
---
Campo de texto de MUI; en este tema su tamaño por defecto es `small` (compacto) y el borde de foco es verde de marca.

## Uso en Patrimonio

- Siempre con `label` en español; `required` muestra el asterisco.
- `fullWidth` dentro de un `Grid` (`<Grid size={{ xs: 12, md: 4 }}>`).
- Errores del backend: `error` + `helperText` con el mensaje.
- `select` + `MenuItem` para catálogos cortos (estado, rol).
- Fechas: `type="date"` con `slotProps={{ inputLabel: { shrink: true } }}`.
- Montos: `type="number"` con `slotProps={{ input: { startAdornment: <InputAdornment position="start">$</InputAdornment> } }}`.
- `multiline minRows={2}` para observaciones y motivos.
- En MUI v9 usa `slotProps`; `InputProps`/`InputLabelProps` ya no existen.

```jsx
<TextField fullWidth required label="No. de control de inventario" value={valor} onChange={(e) => setValor(e.target.value)} />
```
