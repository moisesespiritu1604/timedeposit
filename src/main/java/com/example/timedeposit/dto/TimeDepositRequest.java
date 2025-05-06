package com.example.timedeposit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TimeDepositRequest {
    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 20, message = "Account number must be between 8 and 20 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Amount must be at least 100.00")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a numeric value with up to 10 digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be at least 0.01")
    @DecimalMax(value = "20.00", message = "Interest rate cannot exceed 20.00")
    @Digits(integer = 2, fraction = 2, message = "Interest rate must be a numeric value with up to 2 digits and 2 decimal places")
    private BigDecimal interestRate;

    @NotNull(message = "Term days is required")
    @Min(value = 30, message = "Term days must be at least 30")
    @Max(value = 3650, message = "Term days cannot exceed 3650")
    @Digits(integer = 4, fraction = 0, message = "Term days must be a numeric value with no decimal places")
    private Integer termDays;
}