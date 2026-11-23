import { Box, Card, Tab, Tabs } from "patrimonio-xochitepec";

export const TiposDeBien = () => (
    <Box sx={{ p: 2, maxWidth: 640 }}>
        <Card>
            <Tabs value="muebles" sx={{ px: 2 }}>
                <Tab value="muebles" label="Bienes muebles" sx={{ fontWeight: 700 }} />
                <Tab value="inmuebles" label="Bienes inmuebles" sx={{ fontWeight: 700 }} />
                <Tab value="vehiculos" label="Vehículos" sx={{ fontWeight: 700 }} />
            </Tabs>
        </Card>
    </Box>
);

export const FiltroEstatus = () => (
    <Box sx={{ p: 2, maxWidth: 640 }}>
        <Card>
            <Tabs value="AUTORIZADA" sx={{ px: 2 }}>
                <Tab value="PENDIENTE" label="Pendientes" sx={{ fontWeight: 700 }} />
                <Tab value="AUTORIZADA" label="Autorizadas" sx={{ fontWeight: 700 }} />
                <Tab value="TODAS" label="Todas" sx={{ fontWeight: 700 }} />
            </Tabs>
        </Card>
    </Box>
);
