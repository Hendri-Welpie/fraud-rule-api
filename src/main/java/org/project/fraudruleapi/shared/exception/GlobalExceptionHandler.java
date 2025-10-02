package org.project.fraudruleapi.shared.exception;

import org.project.fraudruleapi.rules.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleObjectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Document currently locked", List.of(e.getMessage()));
    }

    @ExceptionHandler(ConvertionException.class)
    public ResponseEntity<ErrorResponse> handleConvertionException(ConvertionException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Unable to convert", List.of(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFound e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource was not found", List.of(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", List.of(e.getMessage()));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String message,
                                                             List<String> errors) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .errors(errors)
                .message(message)
                .build(), status);
    }
}
