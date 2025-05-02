package com.example.timedeposit.service;

import com.example.timedeposit.exception.AccountAlreadyExistsException;
import com.example.timedeposit.exception.DuplicateDepositException;
import com.example.timedeposit.model.Customer;
import com.example.timedeposit.model.TimeDeposit;
import com.example.timedeposit.model.TimeDepositRequest;
import com.example.timedeposit.model.TimeDepositResponse;
import com.example.timedeposit.repository.CustomerRepository;
import com.example.timedeposit.repository.TimeDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TimeDepositServiceImpl implements TimeDepositService {

    private final TimeDepositRepository timeDepositRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public TimeDepositServiceImpl(TimeDepositRepository timeDepositRepository, CustomerRepository customerRepository) {
        this.timeDepositRepository = timeDepositRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public TimeDepositResponse registerDeposit(TimeDepositRequest request) {

        final Logger log = LoggerFactory.getLogger(TimeDepositServiceImpl.class);
        // Check if customer exists or create a new one
        Customer customer;
        Optional<Customer> existingCustomer = customerRepository.findByAccountNumber(request.getAccountNumber());

        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
            // If customer exists but with different name, throw exception
            if (!customer.getCustomerName().equals(request.getCustomerName())) {
                throw new AccountAlreadyExistsException("Account number already exists with a different customer name");
            }
            // AQUÍ ES DONDE SE AGREGA LA VALIDACIÓN DE DEPÓSITOS DUPLICADOS
            // Verificar si ya existe un depósito con los mismos parámetros creado recientemente
            LocalDate today = LocalDate.now();
            List<TimeDeposit> existingDeposits = timeDepositRepository.findByCustomer_AccountNumber(request.getAccountNumber());
            
            boolean duplicateExists = existingDeposits.stream()
                .anyMatch(deposit -> 
                    deposit.getAmount().compareTo(request.getAmount()) == 0 &&
                    deposit.getInterestRate().compareTo(request.getInterestRate()) == 0 &&
                    deposit.getTermDays().equals(request.getTermDays()) &&
                    deposit.getApplicationDate().equals(today)
                );
            
            if (duplicateExists) {
                log.warn("Duplicate deposit detected for account {}", request.getAccountNumber());
                throw new DuplicateDepositException("A deposit with identical parameters has already been registered today for this account");
            }
        } else {
            customer = new Customer();
            customer.setAccountNumber(request.getAccountNumber());
            customer.setCustomerName(request.getCustomerName());
            customer = customerRepository.save(customer);
        }

        // Create time deposit
        TimeDeposit timeDeposit = new TimeDeposit();
        timeDeposit.setCustomer(customer);
        timeDeposit.setAmount(request.getAmount());
        timeDeposit.setInterestRate(request.getInterestRate());
        timeDeposit.setTermDays(request.getTermDays());

        // Save to database
        TimeDeposit savedDeposit = timeDepositRepository.save(timeDeposit);

        // Convert entity to response
        return convertToResponse(savedDeposit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositResponse> listTimeDeposits() {
        return timeDepositRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Converts a TimeDeposit entity to a TimeDepositResponse DTO
     */
    private TimeDepositResponse convertToResponse(TimeDeposit timeDeposit) {
        return TimeDepositResponse.builder()
                .id(timeDeposit.getId())
                .accountNumber(timeDeposit.getCustomer().getAccountNumber())
                .customerName(timeDeposit.getCustomer().getCustomerName())
                .amount(timeDeposit.getAmount())
                .interestRate(timeDeposit.getInterestRate())
                .termDays(timeDeposit.getTermDays())
                .applicationDate(timeDeposit.getApplicationDate())
                .maturityDate(timeDeposit.getMaturityDate())
                .interestEarned(timeDeposit.getInterestEarned())
                .status(timeDeposit.getStatus())
                .formattedApplicationDate(timeDeposit.getApplicationDate() != null ? 
                    timeDeposit.getApplicationDate().format(DateTimeFormatter.ISO_DATE) : null)
                .formattedMaturityDate(timeDeposit.getMaturityDate() != null ? 
                    timeDeposit.getMaturityDate().format(DateTimeFormatter.ISO_DATE) : null)
                .build();
    }
}
