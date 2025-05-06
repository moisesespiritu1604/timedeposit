import com.example.timedeposit.controller.TimeDepositController;
import com.example.timedeposit.dto.CustomerDepositResponse;
import com.example.timedeposit.dto.TimeDepositDetailResponse;
import com.example.timedeposit.dto.TimeDepositRequest;
import com.example.timedeposit.dto.TimeDepositResponse;
import com.example.timedeposit.service.TimeDepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TimeDepositControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TimeDepositService timeDepositService;

    @InjectMocks
    private TimeDepositController timeDepositController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Configurar ObjectMapper para manejar tipos de fecha de Java 8
        objectMapper.registerModule(new JavaTimeModule());
        
        mockMvc = MockMvcBuilders.standaloneSetup(timeDepositController).build();
    }

    @Test
    void registerDeposit_ShouldReturnCreated() throws Exception {
        // ARRANGE:
        TimeDepositRequest request = new TimeDepositRequest();
        request.setAccountNumber("12345678");
        request.setCustomerName("John Doe");
        request.setAmount(new BigDecimal("1000"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermDays(90);

        // Crear respuesta con la nueva estructura CustomerDepositResponse
        CustomerDepositResponse.CustomerInfo customerInfo = CustomerDepositResponse.CustomerInfo.builder()
                .id(1L)
                .accountNumber("12345678")
                .customerName("John Doe")
                .build();
        
        TimeDepositResponse depositResponse = TimeDepositResponse.builder()
                .id(1L)
                .amount(new BigDecimal("1000"))
                .interestRate(new BigDecimal("5.0"))
                .termDays(90)
                .applicationDate(LocalDate.now())
                .maturityDate(LocalDate.now().plusDays(90))
                .interestEarned(new BigDecimal("12.33"))
                .status("active")
                .formattedApplicationDate(LocalDate.now().toString())
                .formattedMaturityDate(LocalDate.now().plusDays(90).toString())
                .build();
        
        List<TimeDepositResponse> deposits = new ArrayList<>();
        deposits.add(depositResponse);
        
        CustomerDepositResponse response = CustomerDepositResponse.builder()
                .customer(customerInfo)
                .deposits(deposits)
                .build();

        when(timeDepositService.registerDeposit(any(TimeDepositRequest.class)))
                .thenReturn(response);

        // ACT & ASSERT:
        mockMvc.perform(post("/api/time-deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer").exists())
                .andExpect(jsonPath("$.customer.accountNumber").value("12345678"))
                .andExpect(jsonPath("$.customer.customerName").value("John Doe"))
                .andExpect(jsonPath("$.deposits").isArray())
                .andExpect(jsonPath("$.deposits[0].amount").value(1000));
    }

    @Test
    void listDeposits_ShouldReturnDetailedList() throws Exception {
        // ARRANGE: 
        // Crear una lista de TimeDepositDetailResponse en lugar de TimeDepositResponse
        TimeDepositDetailResponse detailResponse = TimeDepositDetailResponse.builder()
                .id(1L)
                .accountNumber("12345678")
                .customerName("John Doe")
                .amount(new BigDecimal("1000"))
                .interestRate(new BigDecimal("5.0"))
                .termDays(90)
                .applicationDate(LocalDate.now())
                .maturityDate(LocalDate.now().plusDays(90))
                .interestEarned(new BigDecimal("12.33"))
                .status("active")
                .formattedApplicationDate(LocalDate.now().toString())
                .formattedMaturityDate(LocalDate.now().plusDays(90).toString())
                .build();
        
        List<TimeDepositDetailResponse> detailedList = Collections.singletonList(detailResponse);
        
        when(timeDepositService.listDetailedTimeDeposits()).thenReturn(detailedList);

        // ACT & ASSERT:
        mockMvc.perform(get("/api/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").value("12345678"))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$[0].amount").value(1000));
    }
    
    @Test
    void listDeposits_ShouldReturnEmptyList() throws Exception {
        // ARRANGE: 
        when(timeDepositService.listDetailedTimeDeposits()).thenReturn(Collections.emptyList());

        // ACT & ASSERT:
        mockMvc.perform(get("/api/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}