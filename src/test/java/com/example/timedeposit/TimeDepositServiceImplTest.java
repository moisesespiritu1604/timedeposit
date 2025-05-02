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

    // Se crean mocks para simular el comportamiento de los repositorios sin conectarse a una base de datos real
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TimeDepositRepository timeDepositRepository;

    // Se inyectan los mocks dentro de la clase que se va a probar
    @InjectMocks
    private TimeDepositServiceImpl service;

    // Objeto que representa la solicitud de creación de un depósito a plazo
    private TimeDepositRequest request;

    // Método que se ejecuta antes de cada prueba, inicializa los mocks y el objeto de solicitud
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks anotados con @Mock

        // Se prepara una solicitud genérica válida
        request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);
    }

    // ✅ Prueba caso exitoso: el cliente no existe, y se crea un nuevo depósito exitosamente
    @Test
    void testRegisterNewDeposit() {
        // Simula que el cliente aún no existe en la base de datos
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.empty());

        // Simula que el método save retorna el mismo cliente recibido
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Simula que el depósito guardado es retornado tal como fue enviado
        when(timeDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Ejecuta el método de negocio
        TimeDepositResponse response = service.registerDeposit(request);

        // Verifica que los datos principales en la respuesta coincidan con la solicitud
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(new BigDecimal("1000"), response.getAmount());
    }

    // ❌ Prueba caso de error: el número de cuenta ya existe pero con un nombre de cliente diferente
    @Test
    void testDuplicateAccountDifferentNameThrowsException() {
        // Simula que existe un cliente con el mismo número de cuenta pero otro nombre
        Customer existing = new Customer(1L, "12345678", "Jane Smith", new ArrayList<>());
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(existing));

        // Verifica que al registrar el depósito se lance la excepción correspondiente
        assertThrows(AccountAlreadyExistsException.class, () -> service.registerDeposit(request));
    }

    // ❌ Prueba caso de error: se intenta registrar un depósito idéntico al ya existente en el mismo día
    @Test
    void testDuplicateDepositThrowsException() {
        // Cliente existente con los mismos datos de la solicitud
        Customer customer = new Customer(1L, "12345678", "John Doe", new ArrayList<>());

        // Depósito existente idéntico al nuevo, con fecha de hoy
        TimeDeposit existingDeposit = new TimeDeposit();
        existingDeposit.setAmount(request.getAmount());
        existingDeposit.setInterestRate(request.getInterestRate());
        existingDeposit.setTermDays(request.getTermDays());
        existingDeposit.setApplicationDate(LocalDate.now());

        // Simula que el cliente ya existe y tiene depósitos
        when(customerRepository.findByAccountNumber("12345678")).thenReturn(Optional.of(customer));
        when(timeDepositRepository.findByCustomer_AccountNumber("12345678"))
                .thenReturn(List.of(existingDeposit));

        // Verifica que se lance la excepción por depósito duplicado
        assertThrows(DuplicateDepositException.class, () -> service.registerDeposit(request));
    }
}
