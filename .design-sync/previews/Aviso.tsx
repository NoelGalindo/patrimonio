import { Aviso } from "patrimonio-xochitepec";

const noop = () => {};

export const Exito = () => (
    <Aviso aviso={{ tipo: "success", mensaje: "Solicitud enviada a Patrimonio para su autorización." }} onClose={noop} />
);

export const Error = () => (
    <Aviso aviso={{ tipo: "error", mensaje: "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución." }} onClose={noop} />
);

export const Informativo = () => (
    <Aviso aviso={{ tipo: "info", mensaje: "Solo puedes agrupar bienes de la misma área en una solicitud." }} onClose={noop} />
);
