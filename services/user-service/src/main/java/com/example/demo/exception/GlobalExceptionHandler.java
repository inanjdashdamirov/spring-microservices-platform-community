package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("API error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiErrorResponse.of(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        ex.getDetails()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiErrorResponse.of(
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        request.getRequestURI(),
                        details
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(ApiErrorResponse.of(
                        ErrorCode.ACCESS_DENIED,
                        "Access denied",
                        request.getRequestURI(),
                        null
                ));
    }

    @ExceptionHandler({JwtException.class, InvalidBearerTokenException.class})
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            Exception ex,
            HttpServletRequest request
    ) {
        String message = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("expired")
                ? "Token expired"
                : "Invalid token";

        ErrorCode code = message.equals("Token expired")
                ? ErrorCode.TOKEN_EXPIRED
                : ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiErrorResponse.of(code, message, request.getRequestURI(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternal(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiErrorResponse.of(
                        ErrorCode.INTERNAL_ERROR,
                        "Internal server error",
                        request.getRequestURI(),
                        null
                ));
    }
}
