package com.example.timedeposit;

import com.example.timedeposit.controller.TimeDepositController;
import com.example.timedeposit.model.TimeDepositRequest;
import com.example.timedeposit.model.TimeDepositResponse;
import com.example.timedeposit.service.TimeDepositService;

import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimeDepositControllerTest {

    @Mock
    private TimeDepositService timeDepositService;

    @InjectMocks
    private TimeDepositController controller;

    public TimeDepositControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterDeposit() {
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);

        TimeDepositResponse response = new TimeDepositResponse();
        response.setCustomerName("John Doe");

        when(timeDepositService.registerDeposit(request)).thenReturn(response);

        ResponseEntity<TimeDepositResponse> result = controller.registerTimeDeposit(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("John Doe", result.getBody().getCustomerName());
    }

    @Test
    void testListDeposits() {
        when(timeDepositService.listTimeDeposits()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TimeDepositResponse>> result = controller.listTimeDeposits();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
