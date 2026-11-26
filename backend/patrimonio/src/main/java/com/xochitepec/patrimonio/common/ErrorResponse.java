package com.xochitepec.patrimonio.common;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(int status, String mensaje, Map<String, String> campos, Instant fecha) {

    public static ErrorResponse of(int status, String mensaje) {
        return new ErrorResponse(status, mensaje, null, Instant.now());
    }

    public static ErrorResponse of(int status, String mensaje, Map<String, String> campos) {
        return new ErrorResponse(status, mensaje, campos, Instant.now());
    }
}
