import { Autocomplete, TextField } from "@mui/material";
import useFetch from "../hooks/useFetch";

/** Selector de área agrupado por dirección general. `value` y `onChange` trabajan con el id. */
export default function AreaSelect({
    value,
    onChange,
    label = "Área",
    required = false,
    excluirId,
    error,
    helperText,
    disabled,
    size = "small",
    sx,
}) {
    const { data: areas, cargando } = useFetch("/catalogos/areas");
    const opciones = (areas ?? []).filter((a) => a.id !== excluirId);
    const seleccionada = opciones.find((a) => a.id === Number(value)) ?? null;

    return (
        <Autocomplete
            options={opciones}
            loading={cargando}
            value={seleccionada}
            disabled={disabled}
            groupBy={(a) => a.direccionGeneral.nombre}
            getOptionLabel={(a) => a.nombre}
            isOptionEqualToValue={(a, b) => a.id === b.id}
            onChange={(_, a) => onChange(a ? a.id : "")}
            noOptionsText="No hay áreas registradas"
            sx={sx}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label={label}
                    required={required}
                    error={error}
                    helperText={helperText}
                    size={size}
                />
            )}
        />
    );
}
