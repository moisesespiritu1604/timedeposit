package com.example.timedeposit.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    void handleAccountAlreadyExistsException() {
        // Arrange
        String errorMessage = "Account already exists";
        AccountAlreadyExistsException ex = new AccountAlreadyExistsException(errorMessage);
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAccountAlreadyExistsException(ex, webRequest);

        // Assert
        assertErrorResponse(response, HttpStatus.CONFLICT, "Account Already Exists", errorMessage);
    }

    @Test
    void handleDuplicateDepositException() {
        // Arrange
        String errorMessage = "Duplicate deposit";
        DuplicateDepositException ex = new DuplicateDepositException(errorMessage);
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleDuplicateDepositException(ex, webRequest);

        // Assert
        assertErrorResponse(response, HttpStatus.CONFLICT, "Duplicate Deposit", errorMessage);
    }

    @Test
    void handleMethodArgumentTypeMismatchException() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("termDays");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleMethodArgumentTypeMismatchException(ex, webRequest);

        // Assert
        String expectedMessage = "Invalid value for parameter 'termDays'. Expected type: Integer.";
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Type Mismatch", expectedMessage);
    }

    @Test
    void handleConstraintViolationException() {
        // Arrange
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        when(ex.getMessage()).thenReturn("Validation failed");
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleConstraintViolationException(ex, webRequest);

        // Assert
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Validation Failed", "Validation failed");
    }

    @Test
    void handleValidationExceptions() throws Exception {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "amount", "must be positive");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleValidationExceptions(ex, webRequest);

        // Assert
        String expectedMessage = "amount: must be positive; ";
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Validation Failed", expectedMessage);
    }

    @Test
    void handleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        // Crear la causa manualmente
        InvalidFormatException cause = new InvalidFormatException(null, "abc", BigDecimal.class);
        // Inyectar el path (campo con error) manualmente
        cause.prependPath(Object.class, "interestRate");

        when(ex.getMostSpecificCause()).thenReturn(cause);
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleHttpMessageNotReadableException(ex, webRequest);

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Invalid Request Format",
                "Invalid value 'abc' for field 'interestRate'. Expected a numeric value."
        );
    }


    @Test
    void handleGlobalException() {
        // Arrange
        Exception ex = new Exception("Unexpected error");
        when(webRequest.getDescription(false)).thenReturn("test-path");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected error"
        );
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response,
                                     HttpStatus expectedStatus,
                                     String expectedError,
                                     String expectedMessage) {
        // Verify status code
        assertEquals(expectedStatus, response.getStatusCode());

        // Verify response body
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(expectedStatus.value(), body.getStatus());
        assertEquals(expectedError, body.getError());
        assertEquals(expectedMessage, body.getMessage());
        assertTrue(body.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}