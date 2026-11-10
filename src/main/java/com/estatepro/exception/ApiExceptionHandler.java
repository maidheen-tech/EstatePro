package com.estatepro.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String, Object>> forbidden(SecurityException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors())
            fields.put(error.getField(), error.getDefaultMessage());
        var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", Instant.now()); body.put("status", 400);
        body.put("error", "Validation failed"); body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException ex) {
        return response(HttpStatus.CONFLICT, "The requested change conflicts with existing data");
    }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", Instant.now()); body.put("status", status.value()); body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
