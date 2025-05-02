package com.example.timedeposit.controller;
import com.example.timedeposit.model.TimeDepositRequest;
import com.example.timedeposit.model.TimeDepositResponse;
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
     * @return Registered time deposit
     */
    @PostMapping // Register a new time deposit
    public ResponseEntity<TimeDepositResponse> registerTimeDeposit(@Valid @RequestBody TimeDepositRequest request) {
        TimeDepositResponse response = timeDepositService.registerDeposit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    /**
     * Endpoint to list all time deposits
     * @return List of time deposits
     */
    @GetMapping
    public ResponseEntity<List<TimeDepositResponse>> listTimeDeposits() {
        List<TimeDepositResponse> deposits = timeDepositService.listTimeDeposits();
        return ResponseEntity.ok(deposits);
    }
}
