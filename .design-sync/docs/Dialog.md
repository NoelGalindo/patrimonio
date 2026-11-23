---
category: Controles MUI
---
Ventana modal de MUI para formularios y fichas de detalle; en celular se abre a pantalla completa (`fullScreen={esMovil}`).

## Uso en Patrimonio

- Compón con `DialogTitle` (`sx={{ fontWeight: 800 }}`), `DialogContent dividers` y `DialogActions sx={{ px: 3, py: 1.5 }}`.
- Formularios: `maxWidth="sm" fullWidth`; fichas de detalle: `maxWidth="md" fullWidth`.
- Botones: Cancelar (`text`) a la izquierda, acción principal (`contained`) al final.
- Para confirmar una acción usa `ConfirmDialog`, no un `Dialog` nuevo.

```jsx
<Dialog open onClose={cerrar} maxWidth="sm" fullWidth>
  <DialogTitle sx={{ fontWeight: 800 }}>Nuevo usuario de área</DialogTitle>
  <DialogContent dividers>{/* campos */}</DialogContent>
  <DialogActions sx={{ px: 3, py: 1.5 }}>
    <Button onClick={cerrar}>Cancelar</Button>
    <Button variant="contained">Guardar</Button>
  </DialogActions>
</Dialog>
```
