package com.xochitepec.patrimonio.common;

import org.springframework.http.HttpStatus;

/** Error de negocio que se traduce directamente a una respuesta HTTP. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String recurso) {
        return new ApiException(HttpStatus.NOT_FOUND, recurso + " no encontrado");
    }

    public static ApiException solicitudInvalida(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException prohibido(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, mensaje);
    }
}
