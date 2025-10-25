package com.techcorp.employee;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.ApiService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.List;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final List<Employee> xmlEmployees;

    public EmployeeManagementApplication(EmployeeService employeeService,
                                         ImportService importService,
                                         ApiService apiService,
                                         @Qualifier("xmlEmployees") List<Employee> xmlEmployees) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.apiService = apiService;
        this.xmlEmployees = xmlEmployees;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== EmployeeManagementApplication start ===");

        // 1) Import z pliku CSV
        var summary = importService.importFromCsv(); // importuje plik z application.properties
        System.out.println("Import summary: " + summary);

        // 2) Dodaj pracowników z XML
        employeeService.addEmployees(xmlEmployees);
        System.out.println("Added " + xmlEmployees.size() + " employees from XML beans.");

        // 3) Pobierz pracowników z API
        try {
            var apiEmployees = apiService.fetchEmployeesFromApi();
            employeeService.addEmployees(apiEmployees);
            System.out.println("Fetched and added " + apiEmployees.size() + " employees from API.");
        } catch (Exception ex) {
            System.err.println("Failed to fetch employees from API: " + ex.getMessage());
        }

        // 4) Pokaż statystyki dla firmy TechCorp
        var stats = employeeService.getCompanyStatistics("TechCorp");
        System.out.println("Company stats: " + stats);

        // 5) Walidacja płac (przykład: list employees below base salary for their position)
        var belowBase = employeeService.validateSalaries();
        System.out.println("Employees below base salary count: " + belowBase.size());
        belowBase.forEach(e -> System.out.println(" - " + e));

        System.out.println("=== EmployeeManagementApplication end ===");
    }
}