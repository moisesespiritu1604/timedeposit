package com.example.timedeposit.controller;

import com.example.timedeposit.model.TimeDepositRequest;
import com.example.timedeposit.model.TimeDepositResponse;
import com.example.timedeposit.service.TimeDepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // Extiende el test con Mockito para habilitar anotaciones de mocks
class TimeDepositControllerTest {

    // MockMvc simula llamadas HTTP a nuestro controlador sin levantar un servidor real
    private MockMvc mockMvc;

    // Se crea un objeto simulado (mock) del servicio que utiliza el controlador.
    @Mock
    private TimeDepositService timeDepositService;

    // Se inyectan los mocks en la instancia real del controlador
    @InjectMocks
    private TimeDepositController timeDepositController;

    // ObjectMapper se utiliza para convertir objetos Java a JSON y viceversa.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Este método se ejecuta antes de cada test para configurar MockMvc con el controlador inyectado
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(timeDepositController).build();
    }

    // Prueba que valida el registro de un depósito exitoso
    @Test
    void registerDeposit_ShouldReturnCreated() throws Exception {
        // ARRANGE:
        // Se crea un objeto de solicitud (request) con los datos necesarios
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);

        // Se crea un objeto de respuesta (response) simulado con el resultado esperado
        TimeDepositResponse response = new TimeDepositResponse();
        response.setCustomerName("John Doe");

        // Se configura el comportamiento del mock: cuando se invoque registerDeposit con cualquier TimeDepositRequest,
        // se retornará el objeto response simulado.
        when(timeDepositService.registerDeposit(any(TimeDepositRequest.class)))
                .thenReturn(response);

        // ACT & ASSERT:
        // Se realiza una llamada HTTP POST al endpoint "/api/time-deposits" con el contenido JSON del request
        // y se verifican los resultados:
        // - status().isCreated(): se espera un código HTTP 201 (CREATED)
        // - jsonPath("$.customerName") valida que en la respuesta, el campo "customerName" tenga el valor "John Doe"
        mockMvc.perform(post("/api/time-deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    // Prueba que valida que al solicitar la lista de depósitos se retorna un array (en este caso, vacío)
    @Test
    void listDeposits_ShouldReturnEmptyList() throws Exception {
        // ARRANGE: se configura el servicio simulado para que retorne una lista vacía al invocar listTimeDeposits()
        when(timeDepositService.listTimeDeposits()).thenReturn(Collections.emptyList());

        // ACT & ASSERT: Se realiza una llamada HTTP GET al endpoint "/api/time-deposits" y se valida:
        // - El código HTTP 200 (OK)
        // - Que el contenido sea de tipo JSON
        // - Que la respuesta sea un arreglo JSON
        mockMvc.perform(get("/api/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }
}
