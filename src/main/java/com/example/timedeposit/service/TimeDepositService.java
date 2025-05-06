package com.example.timedeposit.service;

import com.example.timedeposit.model.CustomerResponse;
import com.example.timedeposit.model.TimeDepositRequest;
import com.example.timedeposit.model.TimeDepositResponse;

import java.util.List;

public interface TimeDepositService {
    /**
     * Registers a new time deposit request
     * @param request Request data
     * @return Response with the registered deposit data
     */
    CustomerResponse registerDeposit(TimeDepositRequest request);

    /**
     * Gets all time deposit requests
     * @return List of time deposits
     */
    List<TimeDepositResponse> listTimeDeposits();

}