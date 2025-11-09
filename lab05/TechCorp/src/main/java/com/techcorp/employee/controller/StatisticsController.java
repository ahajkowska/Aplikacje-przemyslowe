package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler REST dla statystyk pracowników.
 * Udostępnia endpointy do pobierania różnych statystyk i analiz.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final EmployeeService employeeService;

    public StatisticsController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/statistics/salary/average - zwraca średnie wynagrodzenie
     * GET /api/statistics/salary/average?company=X - średnie wynagrodzenie w konkretnej firmie
     * 
     * @param company opcjonalny parametr - nazwa firmy
     * @return Map z kluczem "averageSalary" i wartością średniego wynagrodzenia
     */
    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> getAverageSalary(
            @RequestParam(required = false) String company) {
        
        double averageSalary;
        
        if (company != null && !company.isBlank()) {
            averageSalary = employeeService.averageSalaryByCompany(company);
        } else {
            averageSalary = employeeService.averageSalary();
        }
        
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", averageSalary);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/statistics/company/{companyName} - szczegółowe statystyki firmy
     * 
     * @param companyName nazwa firmy
     * @return CompanyStatisticsDTO ze szczegółowymi statystykami lub 404 jeśli firma nie istnieje
     */
    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(
            @PathVariable String companyName) {
        
        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();
        CompanyStatistics stats = allStats.get(companyName);
        
        if (stats == null) {
            throw new EmployeeNotFoundException("Company '" + companyName + "' not found");
        }
        
        // Pobieramy najwyższe wynagrodzenie w firmie
        Double highestSalary = employeeService.getHighestSalaryInCompany(companyName);
        
        CompanyStatisticsDTO dto = CompanyStatisticsDTO.fromCompanyStatistics(
            companyName, 
            stats, 
            highestSalary
        );
        
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/statistics/positions - liczba pracowników na każdym stanowisku
     * 
     * @return Map z kluczem będącym nazwą stanowiska i wartością będącą liczbą pracowników
     */
    @GetMapping("/positions")
    public ResponseEntity<Map<String, Long>> getEmployeeCountByPosition() {
        Map<Position, Long> positionCounts = employeeService.countEmployeesOnPositions();
        
        // Konwertujemy Position enum na String dla lepszej czytelności w JSON
        Map<String, Long> response = positionCounts.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().name(),
                    Map.Entry::getValue
                ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/statistics/status - rozkład pracowników według statusu zatrudnienia
     * 
     * @return Map z kluczem będącym statusem i wartością będącą liczbą pracowników
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> getEmployeeCountByStatus() {
        // Zakładamy, że wszyscy pracownicy mają status "ACTIVE"
        // W przyszłości można rozszerzyć model Employee o pole status
        Map<String, Long> statusCounts = employeeService.getEmployeeStatusDistribution();
        
        return ResponseEntity.ok(statusCounts);
    }
}
