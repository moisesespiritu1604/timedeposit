package com.example.timedeposit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeDepositResponse {
    private Long id;
    private String accountNumber;
    private String customerName;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termDays;
    private LocalDate applicationDate;
    private LocalDate maturityDate;
    private BigDecimal interestEarned;
    private String status;

    // Formatted dates in ISO format (YYYY-MM-DD)
    private String formattedApplicationDate;
    private String formattedMaturityDate;
}