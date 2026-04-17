package org.project.fraudruleapi.shared.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.fraudruleapi.rules.model.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleConversionException_shouldReturnBadRequest() {
        ConversionException ex = new ConversionException("Conversion failed");

        ResponseEntity<ErrorResponse> response = handler.handleConversionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unable to convert data", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFound_shouldReturnNotFound() {
        ResourceNotFound ex = new ResourceNotFound("Resource not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource was not found", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleDataAccessException_shouldReturnServiceUnavailable() {
        DataAccessException ex = new DataAccessException("DB error") {};

        ResponseEntity<ErrorResponse> response = handler.handleDataAccessException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Database error occurred", response.getBody().getMessage());
    }

    @Test
    void handleException_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
    }
}
