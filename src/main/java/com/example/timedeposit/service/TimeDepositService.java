package com.example.timedeposit.service;

import java.util.List;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;


public interface TimeDepositService {
    /**
     * Registers a new time deposit request
     * @param request Request data
     * @return Response with customer and deposit information
     */
    CustomerDepositResponse registerDeposit(TimeDepositRequest request);

    /**
     * Gets all time deposits with basic information
     * @return List of time deposits
     */
    List<TimeDepositDetailResponse> listDetailedTimeDeposits();
}