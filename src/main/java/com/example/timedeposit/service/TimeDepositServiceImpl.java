package com.example.timedeposit.service;

import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.dto.TimeDepositResponse;
import com.example.timedeposit.exception.AccountAlreadyExistsException;
import com.example.timedeposit.exception.DuplicateDepositException;
import com.example.timedeposit.model.*;
import com.example.timedeposit.repository.CustomerRepository;
import com.example.timedeposit.repository.TimeDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TimeDepositServiceImpl implements TimeDepositService {

    private static final Logger log = LoggerFactory.getLogger(TimeDepositServiceImpl.class);
    
    private final TimeDepositRepository timeDepositRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public TimeDepositServiceImpl(TimeDepositRepository timeDepositRepository, CustomerRepository customerRepository) {
        this.timeDepositRepository = timeDepositRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerDepositResponse registerDeposit(TimeDepositRequest request) {

        Customer customer;
        Optional<Customer> existingCustomer = customerRepository.findByAccountNumber(request.getAccountNumber());

        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
            if (!customer.getCustomerName().equals(request.getCustomerName())) {
                throw new AccountAlreadyExistsException("Account number already exists with a different customer name");
            }
            // **Se eliminó el bloque de verificación de depósitos duplicados**
        } else {
            customer = new Customer();
            customer.setAccountNumber(request.getAccountNumber());
            customer.setCustomerName(request.getCustomerName());
            customer = customerRepository.save(customer);
        }

        TimeDeposit timeDeposit = new TimeDeposit();
        timeDeposit.setCustomer(customer);
        timeDeposit.setAmount(request.getAmount());
        timeDeposit.setInterestRate(request.getInterestRate());
        timeDeposit.setTermDays(request.getTermDays());

        TimeDeposit savedDeposit = timeDepositRepository.save(timeDeposit);

        List<TimeDeposit> customerDeposits = timeDepositRepository.findByCustomer_AccountNumber(customer.getAccountNumber());

        CustomerDepositResponse.CustomerInfo customerInfo = CustomerDepositResponse.CustomerInfo.builder()
                .id(customer.getId())
                .accountNumber(customer.getAccountNumber())
                .customerName(customer.getCustomerName())
                .build();

        List<TimeDepositResponse> depositResponses = customerDeposits.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return CustomerDepositResponse.builder()
                .customer(customerInfo)
                .deposits(depositResponses)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositDetailResponse> listDetailedTimeDeposits() {
        List<TimeDeposit> allDeposits = timeDepositRepository.findAll();
        
        return allDeposits.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a TimeDeposit entity to a TimeDepositResponse DTO
     */
    private TimeDepositResponse convertToResponse(TimeDeposit deposit) {
        return TimeDepositResponse.builder()
                .id(deposit.getId())
                .amount(deposit.getAmount())
                .interestRate(deposit.getInterestRate())
                .termDays(deposit.getTermDays())
                .applicationDate(deposit.getApplicationDate())
                .maturityDate(deposit.getMaturityDate())
                .interestEarned(deposit.getInterestEarned())
                .status(deposit.getStatus())
                .build();
    }
    
    /**
     * Converts a TimeDeposit entity to a TimeDepositDetailResponse DTO with customer information
     */
    private TimeDepositDetailResponse convertToDetailedResponse(TimeDeposit deposit) {
        return TimeDepositDetailResponse.builder()
                .id(deposit.getId())
                .accountNumber(deposit.getCustomer().getAccountNumber())
                .customerName(deposit.getCustomer().getCustomerName())
                .amount(deposit.getAmount())
                .interestRate(deposit.getInterestRate())
                .termDays(deposit.getTermDays())
                .applicationDate(deposit.getApplicationDate())
                .maturityDate(deposit.getMaturityDate())
                .interestEarned(deposit.getInterestEarned())
                .status(deposit.getStatus())
                .build();
    }
    
}
