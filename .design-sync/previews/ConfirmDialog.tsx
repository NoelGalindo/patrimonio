import { ConfirmDialog } from "patrimonio-xochitepec";

const noop = () => {};

export const RechazarConMotivo = () => (
    <ConfirmDialog
        open
        titulo="Rechazar traspaso"
        mensaje="Indica el motivo del rechazo; el área solicitante podrá verlo."
        textoConfirmar="Rechazar"
        color="error"
        comentario="obligatorio"
        etiquetaComentario="Motivo del rechazo"
        onConfirm={noop}
        onClose={noop}
    />
);

export const Autorizar = () => (
    <ConfirmDialog
        open
        titulo="Autorizar traspaso"
        mensaje="Los bienes pasarán al área destino con el nuevo resguardante y se asignará folio al vale."
        textoConfirmar="Autorizar"
        color="success"
        comentario="opcional"
        onConfirm={noop}
        onClose={noop}
    />
);

export const DarDeBaja = () => (
    <ConfirmDialog
        open
        titulo="Dar de baja bien mueble"
        mensaje="¿Confirmas la baja de 0625-c — MONITOR? El registro se conserva en el historial de bajas."
        textoConfirmar="Dar de baja"
        color="error"
        onConfirm={noop}
        onClose={noop}
    />
);
