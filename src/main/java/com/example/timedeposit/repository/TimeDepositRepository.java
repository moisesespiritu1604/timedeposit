package com.example.timedeposit.repository;

import com.example.timedeposit.model.TimeDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeDepositRepository extends JpaRepository<TimeDeposit, Long> {
    List<TimeDeposit> findByCustomer_AccountNumber(String accountNumber);
    List<TimeDeposit> findByStatus(String status);
}
