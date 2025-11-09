package com.techcorp.employee.controller;

import com.techcorp.employee.exception.GlobalExceptionHandler;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy kontrolera StatisticsController używające @WebMvcTest i MockMvc.
 */
@WebMvcTest
@ContextConfiguration(classes = {StatisticsController.class, GlobalExceptionHandler.class})
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    /**
     * Test GET /api/statistics/salary/average - średnie wynagrodzenie w całej organizacji
     */
    @Test
    void testGetAverageSalary_AllEmployees_ReturnsAverageSalary() throws Exception {
        // Given
        when(employeeService.averageSalary()).thenReturn(10500.0);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageSalary").value(10500.0));

        verify(employeeService, times(1)).averageSalary();
        verify(employeeService, never()).averageSalaryByCompany(anyString());
    }

    /**
     * Test GET /api/statistics/salary/average?company=X - średnie wynagrodzenie w firmie
     */
    @Test
    void testGetAverageSalary_ByCompany_ReturnsCompanyAverageSalary() throws Exception {
        // Given
        when(employeeService.averageSalaryByCompany("TechCorp")).thenReturn(11500.0);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageSalary").value(11500.0));

        verify(employeeService, times(1)).averageSalaryByCompany("TechCorp");
        verify(employeeService, never()).averageSalary();
    }

    /**
     * Test GET /api/statistics/company/{companyName} - szczegółowe statystyki firmy
     */
    @Test
    void testGetCompanyStatistics_ExistingCompany_ReturnsStatistics() throws Exception {
        // Given
        CompanyStatistics stats = new CompanyStatistics(15L, 11500.0, "Jan Kowalski");
        
        Map<String, CompanyStatistics> allStats = new HashMap<>();
        allStats.put("TechCorp", stats);
        
        when(employeeService.getCompanyStatistics()).thenReturn(allStats);
        when(employeeService.getHighestSalaryInCompany("TechCorp")).thenReturn(25000.0);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/TechCorp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.employeeCount").value(15))
                .andExpect(jsonPath("$.averageSalary").value(11500.0))
                .andExpect(jsonPath("$.highestSalary").value(25000.0))
                .andExpect(jsonPath("$.topEarnerName").value("Jan Kowalski"));

        verify(employeeService, times(1)).getCompanyStatistics();
        verify(employeeService, times(1)).getHighestSalaryInCompany("TechCorp");
    }

    /**
     * Test GET /api/statistics/company/{companyName} - firma nie istnieje
     */
    @Test
    void testGetCompanyStatistics_NonExistingCompany_Returns404() throws Exception {
        // Given
        Map<String, CompanyStatistics> allStats = new HashMap<>();
        when(employeeService.getCompanyStatistics()).thenReturn(allStats);

        // When & Then
        mockMvc.perform(get("/api/statistics/company/NonExistentCorp"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Company 'NonExistentCorp' not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/statistics/company/NonExistentCorp"));

        verify(employeeService, times(1)).getCompanyStatistics();
        verify(employeeService, never()).getHighestSalaryInCompany(anyString());
    }

    /**
     * Test GET /api/statistics/positions - liczba pracowników na każdym stanowisku
     */
    @Test
    void testGetEmployeeCountByPosition_ReturnsPositionCounts() throws Exception {
        // Given
        Map<Position, Long> positionCounts = new HashMap<>();
        positionCounts.put(Position.DEVELOPER, 10L);
        positionCounts.put(Position.MANAGER, 3L);
        positionCounts.put(Position.PRESIDENT, 1L);
        positionCounts.put(Position.VICE_PRESIDENT, 2L);
        positionCounts.put(Position.INTERN, 5L);
        
        when(employeeService.countEmployeesOnPositions()).thenReturn(positionCounts);

        // When & Then
        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.DEVELOPER").value(10))
                .andExpect(jsonPath("$.MANAGER").value(3))
                .andExpect(jsonPath("$.PRESIDENT").value(1))
                .andExpect(jsonPath("$.VICE_PRESIDENT").value(2))
                .andExpect(jsonPath("$.INTERN").value(5));

        verify(employeeService, times(1)).countEmployeesOnPositions();
    }

    /**
     * Test GET /api/statistics/status - rozkład pracowników według statusu
     */
    @Test
    void testGetEmployeeCountByStatus_ReturnsStatusDistribution() throws Exception {
        // Given
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("ACTIVE", 18L);
        statusCounts.put("ON_LEAVE", 2L);
        statusCounts.put("TERMINATED", 1L);
        
        when(employeeService.getEmployeeStatusDistribution()).thenReturn(statusCounts);

        // When & Then
        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ACTIVE").value(18))
                .andExpect(jsonPath("$.ON_LEAVE").value(2))
                .andExpect(jsonPath("$.TERMINATED").value(1));

        verify(employeeService, times(1)).getEmployeeStatusDistribution();
    }

    /**
     * Test GET /api/statistics/status - gdy wszyscy pracownicy są aktywni
     */
    @Test
    void testGetEmployeeCountByStatus_AllActive_ReturnsOnlyActiveStatus() throws Exception {
        // Given
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("ACTIVE", 21L);
        
        when(employeeService.getEmployeeStatusDistribution()).thenReturn(statusCounts);

        // When & Then
        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ACTIVE").value(21))
                .andExpect(jsonPath("$.ON_LEAVE").doesNotExist())
                .andExpect(jsonPath("$.TERMINATED").doesNotExist());

        verify(employeeService, times(1)).getEmployeeStatusDistribution();
    }

    /**
     * Test GET /api/statistics/positions - brak pracowników
     */
    @Test
    void testGetEmployeeCountByPosition_NoEmployees_ReturnsEmptyMap() throws Exception {
        // Given
        Map<Position, Long> positionCounts = new HashMap<>();
        when(employeeService.countEmployeesOnPositions()).thenReturn(positionCounts);

        // When & Then
        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

        verify(employeeService, times(1)).countEmployeesOnPositions();
    }

    /**
     * Test GET /api/statistics/salary/average - brak pracowników (średnia = 0)
     */
    @Test
    void testGetAverageSalary_NoEmployees_ReturnsZero() throws Exception {
        // Given
        when(employeeService.averageSalary()).thenReturn(0.0);

        // When & Then
        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageSalary").value(0.0));

        verify(employeeService, times(1)).averageSalary();
    }
}
