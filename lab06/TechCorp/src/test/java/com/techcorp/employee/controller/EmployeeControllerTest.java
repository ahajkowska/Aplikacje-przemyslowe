package com.techcorp.employee.controller;

import com.techcorp.employee.dto.EmployeeDTO;
import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.exception.EmployeeNotFoundException;
import com.techcorp.employee.exception.GlobalExceptionHandler;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy kontrolera EmployeeController używające @WebMvcTest i MockMvc.
 */
@WebMvcTest
@ContextConfiguration(classes = {EmployeeController.class, GlobalExceptionHandler.class})
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    /**
     * Test GET /api/employees - zwraca wszystkich pracowników
     * Weryfikacja statusu 200 i zawartości JSON
     */
    @Test
    void testGetAllEmployees_ReturnsListOfEmployees() throws Exception {
        // Given
        Employee emp1 = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ACTIVE);
        Employee emp2 = new Employee("Anna", "Nowak", "anna@example.com", 
                "DataCorp", Position.MANAGER, 12000, EmploymentStatus.ACTIVE);
        
        List<Employee> employees = Arrays.asList(emp1, emp2);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[0].lastName").value("Kowalski"))
                .andExpect(jsonPath("$[0].email").value("jan@example.com"))
                .andExpect(jsonPath("$[0].company").value("TechCorp"))
                .andExpect(jsonPath("$[0].position").value("DEVELOPER"))
                .andExpect(jsonPath("$[0].salary").value(8000))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].firstName").value("Anna"))
                .andExpect(jsonPath("$[1].email").value("anna@example.com"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    /**
     * Test GET /api/employees?company=X - filtrowanie po firmie
     */
    @Test
    void testGetAllEmployees_WithCompanyFilter_ReturnsFilteredList() throws Exception {
        // Given
        Employee emp1 = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ACTIVE);
        
        List<Employee> employees = Arrays.asList(emp1);
        when(employeeService.findEmployeesInCompany("TechCorp")).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("TechCorp"));

        verify(employeeService, times(1)).findEmployeesInCompany("TechCorp");
        verify(employeeService, never()).getAllEmployees();
    }

    /**
     * Test GET /api/employees/{email} - zwraca konkretnego pracownika
     * Weryfikacja zwróconych danych
     */
    @Test
    void testGetEmployeeByEmail_ExistingEmployee_ReturnsEmployee() throws Exception {
        // Given
        Employee emp = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ACTIVE);
        
        when(employeeService.findEmployeeByEmail("jan@example.com")).thenReturn(Optional.of(emp));

        // When & Then
        mockMvc.perform(get("/api/employees/jan@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.company").value("TechCorp"))
                .andExpect(jsonPath("$.position").value("DEVELOPER"))
                .andExpect(jsonPath("$.salary").value(8000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(employeeService, times(1)).findEmployeeByEmail("jan@example.com");
    }

    /**
     * Test GET /api/employees/{email} - pracownik nie istnieje
     * Weryfikacja 404 Not Found
     */
    @Test
    void testGetEmployeeByEmail_NonExistingEmployee_Returns404() throws Exception {
        // Given
        when(employeeService.findEmployeeByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Employee with email 'nonexistent@example.com' not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/employees/nonexistent@example.com"));

        verify(employeeService, times(1)).findEmployeeByEmail("nonexistent@example.com");
    }

    /**
     * Test POST /api/employees - tworzenie nowego pracownika
     * Weryfikacja 201 Created i nagłówka Location
     */
    @Test
    void testCreateEmployee_ValidData_Returns201WithLocation() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", "DEVELOPER", 8000.0, "ACTIVE");
        
        Employee createdEmployee = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ACTIVE);
        
        when(employeeService.addEmployee(any(Employee.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/employees/jan@example.com"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    /**
     * Test POST /api/employees - duplikat emaila
     * Weryfikacja 409 Conflict
     */
    @Test
    void testCreateEmployee_DuplicateEmail_Returns409() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", "DEVELOPER", 8000.0, "ACTIVE");
        
        when(employeeService.addEmployee(any(Employee.class)))
                .thenThrow(new DuplicateEmailException("jan@example.com"));

        // When & Then
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Employee with email 'jan@example.com' already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/employees"));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    /**
     * Test PUT /api/employees/{email} - aktualizacja pracownika
     */
    @Test
    void testUpdateEmployee_ExistingEmployee_Returns200() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", "MANAGER", 12000.0, "ACTIVE");
        
        Employee updatedEmployee = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.MANAGER, 12000, EmploymentStatus.ACTIVE);
        
        when(employeeService.updateEmployee(eq("jan@example.com"), any(Employee.class)))
                .thenReturn(Optional.of(updatedEmployee));

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.position").value("MANAGER"))
                .andExpect(jsonPath("$.salary").value(12000));

        verify(employeeService, times(1)).updateEmployee(eq("jan@example.com"), any(Employee.class));
    }

    /**
     * Test PUT /api/employees/{email} - pracownik nie istnieje
     */
    @Test
    void testUpdateEmployee_NonExistingEmployee_Returns404() throws Exception {
        // Given
        EmployeeDTO employeeDTO = new EmployeeDTO("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", "MANAGER", 12000.0, "ACTIVE");
        
        when(employeeService.updateEmployee(eq("jan@example.com"), any(Employee.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/employees/jan@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with email 'jan@example.com' not found"));

        verify(employeeService, times(1)).updateEmployee(eq("jan@example.com"), any(Employee.class));
    }

    /**
     * Test DELETE /api/employees/{email} - usuwanie pracownika
     * Weryfikacja 204 No Content
     */
    @Test
    void testDeleteEmployee_ExistingEmployee_Returns204() throws Exception {
        // Given
        when(employeeService.deleteEmployee("jan@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/employees/jan@example.com"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(employeeService, times(1)).deleteEmployee("jan@example.com");
    }

    /**
     * Test DELETE /api/employees/{email} - pracownik nie istnieje
     */
    @Test
    void testDeleteEmployee_NonExistingEmployee_Returns404() throws Exception {
        // Given
        when(employeeService.deleteEmployee("nonexistent@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/employees/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with email 'nonexistent@example.com' not found"));

        verify(employeeService, times(1)).deleteEmployee("nonexistent@example.com");
    }

    /**
     * Test PATCH /api/employees/{email}/status - zmiana statusu pracownika
     */
    @Test
    void testUpdateEmployeeStatus_ValidStatus_Returns200() throws Exception {
        // Given
        Employee updatedEmployee = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ON_LEAVE);
        
        when(employeeService.updateEmployeeStatus(eq("jan@example.com"), eq(EmploymentStatus.ON_LEAVE)))
                .thenReturn(Optional.of(updatedEmployee));

        String statusJson = "{\"status\":\"ON_LEAVE\"}";

        // When & Then
        mockMvc.perform(patch("/api/employees/jan@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("jan@example.com"))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        verify(employeeService, times(1)).updateEmployeeStatus("jan@example.com", EmploymentStatus.ON_LEAVE);
    }

    /**
     * Test PATCH /api/employees/{email}/status - pracownik nie istnieje
     */
    @Test
    void testUpdateEmployeeStatus_NonExistingEmployee_Returns404() throws Exception {
        // Given
        when(employeeService.updateEmployeeStatus(eq("nonexistent@example.com"), any(EmploymentStatus.class)))
                .thenReturn(Optional.empty());

        String statusJson = "{\"status\":\"ON_LEAVE\"}";

        // When & Then
        mockMvc.perform(patch("/api/employees/nonexistent@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with email 'nonexistent@example.com' not found"));

        verify(employeeService, times(1)).updateEmployeeStatus("nonexistent@example.com", EmploymentStatus.ON_LEAVE);
    }

    /**
     * Test GET /api/employees/status/{status} - filtrowanie po statusie
     */
    @Test
    void testGetEmployeesByStatus_ReturnsFilteredList() throws Exception {
        // Given
        Employee emp1 = new Employee("Jan", "Kowalski", "jan@example.com", 
                "TechCorp", Position.DEVELOPER, 8000, EmploymentStatus.ON_LEAVE);
        Employee emp2 = new Employee("Anna", "Nowak", "anna@example.com", 
                "DataCorp", Position.MANAGER, 12000, EmploymentStatus.ON_LEAVE);
        
        List<Employee> employees = Arrays.asList(emp1, emp2);
        when(employeeService.findEmployeesByStatus(EmploymentStatus.ON_LEAVE)).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/employees/status/ON_LEAVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ON_LEAVE"))
                .andExpect(jsonPath("$[1].status").value("ON_LEAVE"));

        verify(employeeService, times(1)).findEmployeesByStatus(EmploymentStatus.ON_LEAVE);
    }
}
