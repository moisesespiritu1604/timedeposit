package com.example.timedeposit;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.dto.TimeDepositResponse;
import com.example.timedeposit.exception.AccountAlreadyExistsException;
import com.example.timedeposit.exception.DuplicateDepositException;
import com.example.timedeposit.exception.InvalidNumericValueException;
import com.example.timedeposit.model.*;
import com.example.timedeposit.repository.CustomerRepository;
import com.example.timedeposit.repository.TimeDepositRepository;
import com.example.timedeposit.service.TimeDepositServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeDepositServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TimeDepositRepository timeDepositRepository;

    @InjectMocks
    private TimeDepositServiceImpl service;

    private TimeDepositRequest request;
    private Customer customer;
    private TimeDeposit timeDeposit;

    @BeforeEach
    void setup() {
        // Inicializar la solicitud de prueba
        request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);
        
        // Inicializar el cliente de prueba
        customer = new Customer();
        customer.setId(1L);
        customer.setAccountNumber("12345678");
        customer.setCustomerName("John Doe");
        customer.setTimeDeposits(new ArrayList<>());
        
        // Inicializar el depósito de prueba
        timeDeposit = new TimeDeposit();
        timeDeposit.setId(1L);
        timeDeposit.setCustomer(customer);
        timeDeposit.setAmount(new BigDecimal("1000"));
        timeDeposit.setInterestRate(new BigDecimal("5.0"));
        timeDeposit.setTermDays(90);
        timeDeposit.setApplicationDate(LocalDate.now());
        timeDeposit.setMaturityDate(LocalDate.now().plusDays(90));
        timeDeposit.setInterestEarned(new BigDecimal("12.33"));
        timeDeposit.setStatus("active");
    }

    @Test
    void testRegisterNewDeposit() {
        // Simular que el cliente no existe
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.empty());
        
        // Simular que el cliente se guarda correctamente
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        
        // Simular que el depósito se guarda correctamente
        when(timeDepositRepository.save(any(TimeDeposit.class))).thenReturn(timeDeposit);
        
        // Simular que se encuentra el depósito al buscar por número de cuenta
        when(timeDepositRepository.findByCustomer_AccountNumber("12345678"))
                .thenReturn(List.of(timeDeposit));

        // Ejecutar el método a probar
        CustomerDepositResponse response = service.registerDeposit(request);

        // Verificar el resultado
        assertNotNull(response);
        assertNotNull(response.getCustomer());
        assertEquals("12345678", response.getCustomer().getAccountNumber());
        assertEquals("John Doe", response.getCustomer().getCustomerName());
        assertNotNull(response.getDeposits());
        assertFalse(response.getDeposits().isEmpty());
        assertEquals(new BigDecimal("1000"), response.getDeposits().get(0).getAmount());
        assertEquals(new BigDecimal("5.0"), response.getDeposits().get(0).getInterestRate());
        assertEquals(90, response.getDeposits().get(0).getTermDays());
        
        // Verificar que se llamaron los métodos del repositorio
        verify(customerRepository).findByAccountNumber("12345678");
        verify(customerRepository).save(any(Customer.class));
        verify(timeDepositRepository).save(any(TimeDeposit.class));
        verify(timeDepositRepository).findByCustomer_AccountNumber("12345678");
    }

    @Test
    void testDuplicateAccountDifferentNameThrowsException() {
        // Crear un cliente existente con el mismo número de cuenta pero diferente nombre
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setAccountNumber("12345678");
        existingCustomer.setCustomerName("Jane Smith"); // Nombre diferente
        existingCustomer.setTimeDeposits(new ArrayList<>());
        
        // Simular que el cliente ya existe
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(existingCustomer));

        // Verificar que se lanza la excepción esperada
        assertThrows(AccountAlreadyExistsException.class, () -> service.registerDeposit(request));
        
        // Verificar que se llamó al método del repositorio
        verify(customerRepository).findByAccountNumber("12345678");
        // Verificar que no se llamaron otros métodos
        verify(customerRepository, never()).save(any(Customer.class));
        verify(timeDepositRepository, never()).save(any(TimeDeposit.class));
    }

    @Test
    void testDuplicateDepositThrowsException() {
        // Simular que el cliente ya existe con el mismo nombre
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(customer));
        
        // Crear un depósito existente con los mismos parámetros
        TimeDeposit existingDeposit = new TimeDeposit();
        existingDeposit.setId(1L);
        existingDeposit.setCustomer(customer);
        existingDeposit.setAmount(new BigDecimal("1000"));
        existingDeposit.setInterestRate(new BigDecimal("5.0"));
        existingDeposit.setTermDays(90);
        existingDeposit.setApplicationDate(LocalDate.now()); // Mismo día
        existingDeposit.setMaturityDate(LocalDate.now().plusDays(90));
        existingDeposit.setInterestEarned(new BigDecimal("12.33"));
        existingDeposit.setStatus("active");
        
        // Simular que ya existe un depósito con los mismos parámetros
        when(timeDepositRepository.findByCustomer_AccountNumber("12345678"))
                .thenReturn(List.of(existingDeposit));

        // Verificar que se lanza la excepción esperada
        assertThrows(DuplicateDepositException.class, () -> service.registerDeposit(request));
        
        // Verificar que se llamaron los métodos del repositorio
        verify(customerRepository).findByAccountNumber("12345678");
        verify(timeDepositRepository).findByCustomer_AccountNumber("12345678");
        // Verificar que no se llamaron otros métodos
        verify(customerRepository, never()).save(any(Customer.class));
        verify(timeDepositRepository, never()).save(any(TimeDeposit.class));
    }
    
    @Test
    void testInvalidNumericValueThrowsException() {
        // Crear una solicitud con un valor no numérico
        TimeDepositRequest invalidRequest = new TimeDepositRequest();
        invalidRequest.setAccountNumber("123ABC456"); // Contiene letras
        invalidRequest.setCustomerName("John Doe");
        invalidRequest.setAmount(new BigDecimal("1000"));
        invalidRequest.setInterestRate(new BigDecimal("5.0"));
        invalidRequest.setTermDays(90);

        // Verificar que se lanza la excepción esperada
        assertThrows(InvalidNumericValueException.class, () -> service.registerDeposit(invalidRequest));
        
        // Verificar que no se llamaron métodos del repositorio
        verify(customerRepository, never()).findByAccountNumber(anyString());
        verify(customerRepository, never()).save(any(Customer.class));
        verify(timeDepositRepository, never()).save(any(TimeDeposit.class));
    }
    
    @Test
    void testListDetailedTimeDeposits() {
        // Crear una lista de depósitos para la prueba
        List<TimeDeposit> deposits = new ArrayList<>();
        deposits.add(timeDeposit);
        
        // Simular que se encuentran depósitos al buscar todos
        when(timeDepositRepository.findAll()).thenReturn(deposits);

        // Ejecutar el método a probar
        List<TimeDepositDetailResponse> result = service.listDetailedTimeDeposits();

        // Verificar el resultado
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        TimeDepositDetailResponse detailResponse = result.get(0);
        assertEquals(1L, detailResponse.getId());
        assertEquals("12345678", detailResponse.getAccountNumber());
        assertEquals("John Doe", detailResponse.getCustomerName());
        assertEquals(new BigDecimal("1000"), detailResponse.getAmount());
        assertEquals(new BigDecimal("5.0"), detailResponse.getInterestRate());
        assertEquals(90, detailResponse.getTermDays());
        assertEquals("active", detailResponse.getStatus());
        
        // Verificar que se llamó al método del repositorio
        verify(timeDepositRepository).findAll();
    }
    
    @Test
    void testListDetailedTimeDeposits_EmptyList() {
        // Simular que no se encuentran depósitos
        when(timeDepositRepository.findAll()).thenReturn(new ArrayList<>());

        // Ejecutar el método a probar
        List<TimeDepositDetailResponse> result = service.listDetailedTimeDeposits();

        // Verificar el resultado
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verificar que se llamó al método del repositorio
        verify(timeDepositRepository).findAll();
    }
}
