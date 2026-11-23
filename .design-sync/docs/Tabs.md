---
category: Controles MUI
---
Pestañas de MUI; en este tema son desplazables por defecto (`variant="scrollable"`) para caber en celular. Se componen con `Tab`.

## Uso en Patrimonio

- Separar tipos de bien (Bienes muebles / Bienes inmuebles / Vehículos) y filtros de estatus (Pendientes / Autorizadas / Todas).
- Van dentro de un `Card`, con `sx={{ px: { xs: 0, sm: 2 } }}`; cada `Tab` lleva `sx={{ fontWeight: 700 }}`.
- El color activo es el verde primario.

```jsx
<Card>
  <Tabs value={tipo} onChange={(_, v) => setTipo(v)}>
    <Tab value="muebles" label="Bienes muebles" sx={{ fontWeight: 700 }} />
    <Tab value="inmuebles" label="Bienes inmuebles" sx={{ fontWeight: 700 }} />
  </Tabs>
</Card>
```
