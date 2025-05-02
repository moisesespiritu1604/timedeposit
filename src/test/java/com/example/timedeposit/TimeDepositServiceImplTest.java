package com.example.timedeposit;

import com.example.timedeposit.exception.AccountAlreadyExistsException;
import com.example.timedeposit.exception.DuplicateDepositException;
import com.example.timedeposit.model.*;
import com.example.timedeposit.repository.CustomerRepository;
import com.example.timedeposit.repository.TimeDepositRepository;
import com.example.timedeposit.service.TimeDepositServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimeDepositServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TimeDepositRepository timeDepositRepository;

    @InjectMocks
    private TimeDepositServiceImpl service;

    private TimeDepositRequest request;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);
    }

    @Test
    void testRegisterNewDeposit() {
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(timeDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeDepositResponse response = service.registerDeposit(request);

        assertEquals("John Doe", response.getCustomerName());
        assertEquals(new BigDecimal("1000"), response.getAmount());
    }

    @Test
    void testDuplicateAccountDifferentNameThrowsException() {
        Customer existing = new Customer(1L, "12345678", "Jane Smith", new ArrayList<>());
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(existing));

        assertThrows(AccountAlreadyExistsException.class, () -> service.registerDeposit(request));
    }

    @Test
    void testDuplicateDepositThrowsException() {
        Customer customer = new Customer(1L, "12345678", "John Doe", new ArrayList<>());

        TimeDeposit existingDeposit = new TimeDeposit();
        existingDeposit.setAmount(request.getAmount());
        existingDeposit.setInterestRate(request.getInterestRate());
        existingDeposit.setTermDays(request.getTermDays());
        existingDeposit.setApplicationDate(LocalDate.now());

        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(customer));
        when(timeDepositRepository.findByCustomer_AccountNumber("12345678")).thenReturn(List.of(existingDeposit));

        assertThrows(DuplicateDepositException.class, () -> service.registerDeposit(request));
    }
}
