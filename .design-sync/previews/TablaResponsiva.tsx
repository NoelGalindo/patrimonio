import { Box, Chip, IconButton, TablaResponsiva, Tooltip } from "patrimonio-xochitepec";

const bienes = [
    { id: 1, inventario: "0625-c", descripcion: "MONITOR", marca: "AOC", serie: "EQUD51A002965", estado: "BUENO", area: "Recursos Humanos", costo: "$1,500.00" },
    { id: 2, inventario: "0626-c", descripcion: "ESCRITORIO EJECUTIVO DE MADERA", marca: "LÍNEA ITALIA", serie: "MUE-99281", estado: "BUENO", area: "Recursos Humanos", costo: "$8,450.50" },
    { id: 3, inventario: "0627-c", descripcion: "SILLA OPERATIVA", marca: "OFFIHO", serie: "CH-10291", estado: "REGULAR", area: "Planeación del Desarrollo Urbano", costo: "$2,300.00" },
];

const columnas = [
    { id: "inv", label: "No. de inventario", render: (b) => b.inventario, principal: true },
    { id: "desc", label: "Descripción", render: (b) => b.descripcion, secundaria: true },
    { id: "marca", label: "Marca", render: (b) => b.marca },
    { id: "serie", label: "Serie", render: (b) => b.serie },
    {
        id: "estado",
        label: "Estado",
        render: (b) => (
            <Chip label={b.estado} size="small" color={b.estado === "BUENO" ? "success" : "warning"} sx={{ fontWeight: 700 }} />
        ),
    },
    { id: "area", label: "Área", render: (b) => b.area },
    { id: "costo", label: "Costo del bien", align: "right", render: (b) => b.costo },
];

const Editar = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
    </svg>
);

export const InventarioConAcciones = () => (
    <Box sx={{ p: 2 }}>
        <TablaResponsiva
            columnas={columnas}
            filas={bienes}
            onClickFila={() => {}}
            acciones={() => (
                <Tooltip title="Editar">
                    <IconButton size="small">
                        <Editar />
                    </IconButton>
                </Tooltip>
            )}
            paginacion={{ count: 3, page: 0, rowsPerPage: 20, rowsPerPageOptions: [10, 20, 50], onPageChange: () => {}, onRowsPerPageChange: () => {} }}
        />
    </Box>
);

export const ConSeleccion = () => (
    <Box sx={{ p: 2 }}>
        <TablaResponsiva
            columnas={columnas.slice(0, 4)}
            filas={bienes}
            seleccion={{ esSeleccionada: (b) => b.id === 2, alternar: () => {} }}
        />
    </Box>
);

export const Cargando = () => (
    <Box sx={{ p: 2 }}>
        <TablaResponsiva columnas={columnas.slice(0, 4)} filas={bienes.slice(0, 1)} cargando />
    </Box>
);

export const SinRegistros = () => (
    <Box sx={{ p: 2 }}>
        <TablaResponsiva columnas={columnas.slice(0, 4)} filas={[]} textoVacio="No se encontraron bienes muebles." />
    </Box>
);
