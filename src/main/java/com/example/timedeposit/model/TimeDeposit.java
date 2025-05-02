package com.example.timedeposit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "time_deposits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Amount must be at least 100.00")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be at least 0.01")
    @DecimalMax(value = "20.00", message = "Interest rate cannot exceed 20.00")
    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @NotNull(message = "Term days is required")
    @Min(value = 30, message = "Term days must be at least 30")
    @Max(value = 3650, message = "Term days cannot exceed 3650")
    @Column(name = "term_days", nullable = false)
    private Integer termDays;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "interest_earned", nullable = false)
    private BigDecimal interestEarned;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        this.applicationDate = LocalDate.now();
        // Calculate maturity date
        this.maturityDate = this.applicationDate.plusDays(termDays);
        this.status = "active"; // Initial status when creating the deposit
        // Calculate interest earned
        this.interestEarned = this.amount
                .multiply(this.interestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .multiply(new BigDecimal(this.termDays))
                .divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
    }

    public String getFormattedApplicationDate() {
        return applicationDate != null ? applicationDate.format(DateTimeFormatter.ISO_DATE) : null;
    }

    public String getFormattedMaturityDate() {
        return maturityDate != null ? maturityDate.format(DateTimeFormatter.ISO_DATE) : null;
    }
}
