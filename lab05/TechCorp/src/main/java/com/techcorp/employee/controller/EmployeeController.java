package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler REST dla operacji na pracownikach.
 * Udostępnia endpointy do zarządzania pracownikami w systemie.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/employees - zwraca listę wszystkich pracowników
     * GET /api/employees?company=X - filtruje pracowników po nazwie firmy
     * 
     * @param company opcjonalny parametr filtrujący po nazwie firmy
     * @return lista pracowników jako EmployeeDTO ze statusem 200 OK
     */
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String company) {
        
        List<Employee> employees;
        
        if (company != null && !company.isBlank()) {
            employees = employeeService.findEmployeesInCompany(company);
        } else {
            employees = employeeService.getAllEmployees();
        }
        
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::fromEmployee)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeDTOs);
    }

    /**
     * GET /api/employees/{email} - zwraca konkretnego pracownika po emailu
     * 
     * @param email adres email pracownika
     * @return EmployeeDTO ze statusem 200 OK jeśli istnieje, 404 Not Found jeśli nie
     */
    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email) {
        Optional<Employee> employee = employeeService.findEmployeeByEmail(email);
        
        if (employee.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with email '" + email + "' not found");
        }
        
        return ResponseEntity.ok(EmployeeDTO.fromEmployee(employee.get()));
    }

    /**
     * POST /api/employees - tworzy nowego pracownika
     * 
     * @param employeeDTO dane nowego pracownika
     * @return 201 Created z nagłówkiem Location oraz utworzonym obiektem w ciele
     */
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee employee = employeeDTO.toEmployee();
        employeeService.addEmployee(employee);
        
        // Tworzymy URI dla nowo utworzonego zasobu
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(employee.getEmail())
                .toUri();
        
        EmployeeDTO createdEmployee = EmployeeDTO.fromEmployee(employee);
        
        return ResponseEntity.created(location).body(createdEmployee);
    }

    /**
     * PUT /api/employees/{email} - aktualizuje dane pracownika
     * 
     * @param email adres email pracownika do aktualizacji
     * @param employeeDTO nowe dane pracownika
     * @return 200 OK z zaktualizowanym obiektem lub 404 Not Found jeśli nie istnieje
     */
    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String email,
            @RequestBody EmployeeDTO employeeDTO) {
        
        Optional<Employee> updatedEmployee = employeeService.updateEmployee(email, employeeDTO.toEmployee());
        
        if (updatedEmployee.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with email '" + email + "' not found");
        }
        
        return ResponseEntity.ok(EmployeeDTO.fromEmployee(updatedEmployee.get()));
    }

    /**
     * DELETE /api/employees/{email} - usuwa pracownika
     * 
     * @param email adres email pracownika do usunięcia
     * @return 204 No Content przy sukcesie lub 404 Not Found jeśli nie istnieje
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        boolean deleted = employeeService.deleteEmployee(email);
        
        if (!deleted) {
            throw new EmployeeNotFoundException("Employee with email '" + email + "' not found");
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/employees/{email}/status - zmienia tylko status pracownika
     * 
     * @param email adres email pracownika
     * @param statusUpdate mapa z kluczem "status" i wartością nowego statusu
     * @return 200 OK z zaktualizowanym pracownikiem lub 404 Not Found
     */
    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
            @PathVariable String email,
            @RequestBody Map<String, String> statusUpdate) {
        
        String statusValue = statusUpdate.get("status");
        if (statusValue == null || statusValue.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        EmploymentStatus newStatus = EmploymentStatus.valueOf(statusValue.toUpperCase());
        Optional<Employee> updatedEmployee = employeeService.updateEmployeeStatus(email, newStatus);
        
        if (updatedEmployee.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with email '" + email + "' not found");
        }
        
        return ResponseEntity.ok(EmployeeDTO.fromEmployee(updatedEmployee.get()));
    }

    /**
     * GET /api/employees/status/{status} - zwraca listę pracowników o danym statusie
     * 
     * @param status status zatrudnienia (ACTIVE, ON_LEAVE, TERMINATED)
     * @return lista pracowników o podanym statusie ze statusem 200 OK
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable String status) {
        EmploymentStatus employmentStatus = EmploymentStatus.valueOf(status.toUpperCase());
        List<Employee> employees = employeeService.findEmployeesByStatus(employmentStatus);
        
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::fromEmployee)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeDTOs);
    }
}
