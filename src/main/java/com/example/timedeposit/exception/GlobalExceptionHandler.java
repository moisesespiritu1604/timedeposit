package com.example.timedeposit.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyExistsException(
            AccountAlreadyExistsException ex, WebRequest request) {

        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Account Already Exists", request);
    }

    @ExceptionHandler(DuplicateDepositException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDepositException(
            DuplicateDepositException ex, WebRequest request) {

        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Duplicate Deposit", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message = String.format("Invalid value for parameter '%s'. Expected type: %s.",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, "Type Mismatch", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation Failed", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        StringBuilder sb = new StringBuilder();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            if (error instanceof FieldError fieldError) {
                sb.append(fieldError.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
            } else {
                sb.append(error.getDefaultMessage()).append("; ");
            }
        }

        return buildErrorResponse(sb.toString(), HttpStatus.BAD_REQUEST, "Validation Failed", request);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        Throwable rootCause = ex.getMostSpecificCause();
        String message;

        if (rootCause instanceof InvalidFormatException formatEx) {
            // Verificar si hay elementos en el path
            if (!formatEx.getPath().isEmpty()) {
                message = String.format("Invalid value '%s' for field '%s'. Expected a numeric value.",
                        formatEx.getValue(),
                        formatEx.getPath().get(0).getFieldName());
            } else {
                message = "Invalid JSON format for an unknown field.";
            }
        } else {
            message = "Malformed JSON request or invalid data type.";
        }

        return buildErrorResponse(
                message,
                HttpStatus.BAD_REQUEST,
                "Invalid Request Format",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", request);
    }

    // MÉTODO AUXILIAR COMÚN
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, String errorTitle, WebRequest request) {

        return buildErrorResponse(ex.getMessage(), status, errorTitle, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String message, HttpStatus status, String errorTitle, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorTitle,
                message,
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
