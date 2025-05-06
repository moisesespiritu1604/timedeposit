package com.example.timedeposit.service;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.exception.AccountAlreadyExistsException;
import com.example.timedeposit.exception.DuplicateDepositException;
import com.example.timedeposit.model.Customer;
import com.example.timedeposit.model.TimeDeposit;
import com.example.timedeposit.repository.CustomerRepository;
import com.example.timedeposit.repository.TimeDepositRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;


@ExtendWith(MockitoExtension.class)
class TimeDepositServiceImplTest {

    @Mock
    private TimeDepositRepository timeDepositRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TimeDepositServiceImpl timeDepositService;

    private TimeDepositRequest validRequest() {
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000.00"));
        request.setInterestRate(new BigDecimal("5.00"));
        request.setTermDays(30);
        return request;
    }

    @Test
    void registerDeposit_NewCustomer() {
        when(customerRepository.findByAccountNumber(any())).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerDepositResponse response = timeDepositService.registerDeposit(validRequest());

        assertNotNull(response.getCustomer());
        assertEquals("12345678", response.getCustomer().getAccountNumber());
        verify(customerRepository).save(any());
    }

    @Test
    void registerDeposit_ExistingCustomerWithDifferentName() {
        Customer existing = new Customer();
        existing.setCustomerName("Different Name");
        existing.setAccountNumber("12345678");

        when(customerRepository.findByAccountNumber(any())).thenReturn(Optional.of(existing));

        assertThrows(AccountAlreadyExistsException.class, () -> {
            timeDepositService.registerDeposit(validRequest());
        });
    }

    @Test
    void registerDeposit_DuplicateDeposit() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("John Doe");
        customer.setAccountNumber("12345678");

        TimeDeposit existingDeposit = new TimeDeposit();
        existingDeposit.setApplicationDate(LocalDate.now());
        existingDeposit.setAmount(new BigDecimal("1000.00"));
        existingDeposit.setInterestRate(new BigDecimal("5.00"));
        existingDeposit.setTermDays(30);

        when(customerRepository.findByAccountNumber(any())).thenReturn(Optional.of(customer));
        when(timeDepositRepository.findByCustomer_AccountNumber(any()))
                .thenReturn(List.of(existingDeposit));

        assertThrows(DuplicateDepositException.class, () -> {
            timeDepositService.registerDeposit(validRequest());
        });
    }

    @Test
    void listDetailedTimeDeposits() {
        TimeDeposit deposit = new TimeDeposit();
        deposit.setCustomer(new Customer());
        deposit.getCustomer().setAccountNumber("12345678");
        deposit.setAmount(new BigDecimal("1000.00"));

        when(timeDepositRepository.findAll()).thenReturn(List.of(deposit));

        List<TimeDepositDetailResponse> result = timeDepositService.listDetailedTimeDeposits();

        assertFalse(result.isEmpty());
        assertEquals("12345678", result.get(0).getAccountNumber());
    }
}