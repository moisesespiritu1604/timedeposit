package com.example.timedeposit.controller;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.exception.GlobalExceptionHandler;
import com.example.timedeposit.service.TimeDepositService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class TimeDepositControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TimeDepositService timeDepositService;

    @InjectMocks
    private TimeDepositController timeDepositController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(timeDepositController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerTimeDeposit() throws Exception {
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000.00"));
        request.setInterestRate(new BigDecimal("5.00"));
        request.setTermDays(30);

        CustomerDepositResponse response = CustomerDepositResponse.builder()
                .customer(CustomerDepositResponse.CustomerInfo.builder()
                        .id(1L)
                        .accountNumber("12345678")
                        .customerName("John Doe")
                        .build())
                .deposits(Collections.emptyList())
                .build();

        when(timeDepositService.registerDeposit(any())).thenReturn(response);

        mockMvc.perform(post("/api/time-deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer.accountNumber").value("12345678"));

        verify(timeDepositService).registerDeposit(any());
    }

    @Test
    void listTimeDeposits() throws Exception {
        TimeDepositDetailResponse deposit = TimeDepositDetailResponse.builder()
                .id(1L)
                .accountNumber("12345678")
                .amount(new BigDecimal("1000.00"))
                .status("active")
                .build();

        when(timeDepositService.listDetailedTimeDeposits()).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("12345678"));

        verify(timeDepositService).listDetailedTimeDeposits();
    }
}