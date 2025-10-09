package com.nhom4.moviereservation.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String code;
    private final int status;
    private final Object data;

    public ApiException(String code, String message, Object data, int status) {
        super(message);
        this.code = code;
        this.status = status;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    // ================== Nested Exception Classes ==================

    public static class InternalServerException extends ApiException {
        public InternalServerException(String message, Object data) {
            super("InternalServerException", message, data, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public static class InvalidEndpointException extends ApiException {
        public InvalidEndpointException(String message, Object data) {
            super("InvalidEndpointException", message, data, HttpStatus.NOT_FOUND.value());
        }
    }

    public static class UnimplementedException extends ApiException {
        public UnimplementedException(String message, Object data) {
            super("UnimplementedException", message, data, HttpStatus.NOT_IMPLEMENTED.value());
        }
    }

    public static class HealthCheckFailedException extends ApiException {
        public HealthCheckFailedException(Object data) {
            super("HealthCheckFailedException", "API failed to run", data, HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }
}
