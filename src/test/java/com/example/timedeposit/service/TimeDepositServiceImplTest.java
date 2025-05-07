package com.example.timedeposit.service;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.dto.TimeDepositResponse;
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
import java.util.concurrent.atomic.AtomicInteger;


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

    private TimeDeposit buildTimeDeposit(LocalDate applicationDate) {
        TimeDeposit td = new TimeDeposit();
        td.setId(10L);
        td.setAmount(new BigDecimal("1000.00"));
        td.setInterestRate(new BigDecimal("5.00"));
        td.setTermDays(30);
        td.setApplicationDate(applicationDate);
        td.setMaturityDate(applicationDate.plusDays(30));
        td.setInterestEarned(new BigDecimal("41.10"));
        td.setStatus("ACTIVE");
        return td;
    }

    @Test
    void registerDeposit_NewCustomer_Success() {
        when(customerRepository.findByAccountNumber(any())).thenReturn(Optional.empty());

        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        when(timeDepositRepository.save(any())).thenAnswer(inv -> {
            TimeDeposit td = inv.getArgument(0);
            td.setId(10L);
            td.setApplicationDate(LocalDate.now());
            td.setMaturityDate(LocalDate.now().plusDays(30));
            td.setInterestEarned(new BigDecimal("41.10"));
            td.setStatus("ACTIVE");
            return td;
        });

        when(timeDepositRepository.findByCustomer_AccountNumber(any()))
                .thenReturn(List.of(buildTimeDeposit(LocalDate.now())));

        CustomerDepositResponse response = timeDepositService.registerDeposit(validRequest());

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomer().getCustomerName());
        assertEquals(1, response.getDeposits().size());
        assertEquals(10L, response.getDeposits().get(0).getId());
    }

    @Test
    void registerDeposit_ExistingCustomerWithDifferentName_ShouldThrow() {
        Customer existing = new Customer();
        existing.setCustomerName("Different Name");
        existing.setAccountNumber("12345678");

        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(existing));

        assertThrows(AccountAlreadyExistsException.class, () -> {
            timeDepositService.registerDeposit(validRequest());
        });
    }

    //nuevos test
    @Test
    void whenCustomerDoesNotExist_thenCreateNewCustomerAndDeposit() {
        // Configurar mocks
        String accountNumber = "ACC-001";
        when(customerRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(timeDepositRepository.save(any(TimeDeposit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(timeDepositRepository.findByCustomer_AccountNumber(accountNumber)).thenReturn(List.of(new TimeDeposit()));

        // Crear solicitud
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber(accountNumber);
        request.setCustomerName("Nuevo Cliente");
        request.setAmount(BigDecimal.valueOf(2000));
        request.setInterestRate(BigDecimal.valueOf(3.5));
        request.setTermDays(60);

        // Ejecutar
        CustomerDepositResponse response = timeDepositService.registerDeposit(request);

        // Verificar
        verify(customerRepository, times(1)).save(any(Customer.class)); // Cliente guardado
        verify(timeDepositRepository, times(1)).save(any(TimeDeposit.class)); // Depósito guardado
        assertNotNull(response.getCustomer());
        assertEquals(accountNumber, response.getCustomer().getAccountNumber());
    }
    @Test
    void whenCustomerExistsAndNameMatches_thenAddDeposit() {
        // Configurar mocks
        String accountNumber = "ACC-002";
        Customer existingCustomer = new Customer();
        existingCustomer.setAccountNumber(accountNumber);
        existingCustomer.setCustomerName("Cliente Existente");

        when(customerRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingCustomer));
        when(timeDepositRepository.save(any(TimeDeposit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(timeDepositRepository.findByCustomer_AccountNumber(accountNumber)).thenReturn(List.of(new TimeDeposit(), new TimeDeposit()));

        // Solicitud con mismo nombre
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber(accountNumber);
        request.setCustomerName("Cliente Existente");
        request.setAmount(BigDecimal.valueOf(1500));

        // Ejecutar y validar
        assertDoesNotThrow(() -> timeDepositService.registerDeposit(request));
        verify(timeDepositRepository, times(1)).save(any(TimeDeposit.class));
        assertEquals(2, timeDepositService.registerDeposit(request).getDeposits().size()); // 2 depósitos en respuesta
    }
    @Test
    void whenCustomerExistsButNameDiffers_thenThrowException() {
        String accountNumber = "ACC-003";
        Customer existingCustomer = new Customer();
        existingCustomer.setAccountNumber(accountNumber);
        existingCustomer.setCustomerName("Nombre Original");

        when(customerRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingCustomer));

        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber(accountNumber);
        request.setCustomerName("Nombre Incorrecto"); // Nombre diferente

        // Verificar excepción
        AccountAlreadyExistsException exception = assertThrows(
                AccountAlreadyExistsException.class,
                () -> timeDepositService.registerDeposit(request)
        );
        assertEquals("Account number already exists with a different customer name", exception.getMessage());
    }

    @Test
    void listDetailedTimeDeposits_ShouldReturnList() {
        Customer customer = new Customer();
        customer.setAccountNumber("12345678");
        customer.setCustomerName("John Doe");

        TimeDeposit deposit = buildTimeDeposit(LocalDate.now());
        deposit.setCustomer(customer);

        when(timeDepositRepository.findAll()).thenReturn(List.of(deposit));

        List<TimeDepositDetailResponse> result = timeDepositService.listDetailedTimeDeposits();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getCustomerName());
        assertEquals("12345678", result.get(0).getAccountNumber());
    }
}
