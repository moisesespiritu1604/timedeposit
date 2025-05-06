package com.example.timedeposit.controller;
import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.CustomerResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.dto.TimeDepositResponse;
import com.example.timedeposit.service.TimeDepositService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-deposits")
public class TimeDepositController {
    private final TimeDepositService timeDepositService;

    @Autowired // Constructor-based dependency injection
    public TimeDepositController(TimeDepositService timeDepositService) {
        this.timeDepositService = timeDepositService;
    }

    /**
     * Endpoint to register a new time deposit
     * @param request Time deposit data
     * @return Customer with the newly registered deposit
     */
    @PostMapping
    public ResponseEntity<CustomerDepositResponse> registerTimeDeposit(@Valid @RequestBody TimeDepositRequest request) {
        CustomerDepositResponse response = timeDepositService.registerDeposit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to list all time deposits with detailed information
     * @return List of time deposits with detailed information
     */
    @GetMapping
    public ResponseEntity<List<TimeDepositDetailResponse>> listTimeDeposits() {
        List<TimeDepositDetailResponse> deposits = timeDepositService.listDetailedTimeDeposits();
        return ResponseEntity.ok(deposits);
    }
}
