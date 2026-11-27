import { Autocomplete, Box, TextField, Typography } from "@mui/material";
import useFetch from "../hooks/useFetch";

/**
 * Selector de cuenta contable (Guía de Vida Útil del CONAC) para un tipo de bien.
 * Solo ofrece cuentas activas con vida útil definida. `value` y `onChange` trabajan con el id.
 */
export default function CuentaContableSelect({
    tipoContable,
    value,
    onChange,
    required = false,
    error,
    helperText,
    disabled,
    sx,
}) {
    const { data: cuentas, cargando } = useFetch("/catalogos/cuentas-contables", { tipoBien: tipoContable });
    const opciones = cuentas ?? [];
    const seleccionada = opciones.find((c) => c.id === Number(value)) ?? null;
    const ayuda = seleccionada
        ? `Vida útil: ${seleccionada.vidaUtilAnios} años · depreciación anual: ${seleccionada.porcentajeAnual} %`
        : "Define la vida útil y la depreciación del bien (Guía CONAC)";

    return (
        <Autocomplete
            options={opciones}
            loading={cargando}
            value={seleccionada}
            disabled={disabled}
            getOptionLabel={(c) => `${c.clave} — ${c.concepto}`}
            isOptionEqualToValue={(a, b) => a.id === b.id}
            onChange={(_, c) => onChange(c ? c.id : "")}
            noOptionsText="No hay cuentas para este tipo de bien"
            sx={sx}
            renderOption={({ key, ...props }, c) => (
                <Box component="li" key={key} {...props} sx={{ flexDirection: "column", alignItems: "flex-start !important" }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {c.clave} — {c.concepto}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                        {c.vidaUtilAnios} años · {c.porcentajeAnual} % anual
                    </Typography>
                </Box>
            )}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label="Cuenta contable"
                    required={required}
                    error={error}
                    helperText={helperText || ayuda}
                    size="small"
                />
            )}
        />
    );
}
