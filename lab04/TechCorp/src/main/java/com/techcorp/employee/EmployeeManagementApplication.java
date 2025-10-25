package com.techcorp.employee;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.ApiService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = "com.techcorp.employee")
public class EmployeeManagementApplication implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final List<Employee> xmlEmployees;

    public EmployeeManagementApplication(
            EmployeeService employeeService,
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
        System.out.println("EMPLOYEE MANAGEMENT APP");
        System.out.println("-".repeat(80));
        System.out.println();

        // 1. IMPORT PRACOWNIKÓW Z PLIKU CSV
        System.out.println("1. IMPORT PRACOWNIKÓW Z PLIKU CSV");
        System.out.println("-".repeat(80));

        String csvPath = "src/main/resources/employees.csv";
        ImportSummary csvImportSummary = importService.importFromCsv(csvPath);

        System.out.println("- Zaimportowano: " + csvImportSummary.getImportedCount() + " pracowników");

        if (!csvImportSummary.getErrors().isEmpty()) {
            System.out.println("- Błędy podczas importu:");
            csvImportSummary.getErrors().forEach((error) ->
                    System.out.println(error)
            );
        }
        System.out.println();

        // 2. DODANIE PRACOWNIKÓW Z BEANA XML
        System.out.println("2. DODANIE PRACOWNIKÓW Z KONFIGURACJI XML");
        System.out.println("-".repeat(80));

        int xmlImportedCount = 0;
        int xmlErrorsCount = 0;

        for (Employee employee : xmlEmployees) {
            try {
                boolean added = employeeService.addEmployee(employee);
                if (added) {
                    xmlImportedCount++;
                    System.out.println("Dodano: " + employee.getFirstName() + " " +
                            employee.getLastName() + " (" + employee.getEmail() + ")");
                }
            } catch (IllegalArgumentException e) {
                xmlErrorsCount++;
                System.out.println("Błąd dla: " + employee.getEmail() + " - " + e.getMessage());
            }
        }

        System.out.println("\nPodsumowanie importu z XML:");
        System.out.println("- Zaimportowano: " + xmlImportedCount + " pracowników");
        System.out.println("- Błędy: " + xmlErrorsCount);
        System.out.println();

        // 3. POBRANIE DANYCH Z REST API
        System.out.println("3. POBIERANIE PRACOWNIKÓW Z REST API");
        System.out.println("-".repeat(80));

        try {
            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi();
            int apiImportedCount = 0;

            for (Employee employee : apiEmployees) {
                try {
                    boolean added = employeeService.addEmployee(employee);
                    if (added) {
                        apiImportedCount++;
                    }
                } catch (IllegalArgumentException e) {
                    // Email już istnieje - pomijamy
                }
            }

            System.out.println("Pobrano z API: " + apiEmployees.size() + " pracowników");
            System.out.println("Dodano do systemu: " + apiImportedCount + " nowych pracowników");
        } catch (Exception e) {
            System.out.println("Błąd podczas pobierania danych z API: " + e.getMessage());
        }
        System.out.println();

        // 4. WYŚWIETLENIE OGÓLNYCH STATYSTYK
        System.out.println("4. OGÓLNE STATYSTYKI SYSTEMU");
        System.out.println("-".repeat(80));

        List<Employee> allEmployees = employeeService.getAllEmployees();
        System.out.println("Łączna liczba pracowników w systemie: " + allEmployees.size());
        System.out.println("Średnie wynagrodzenie: " + String.format("%.2f", employeeService.averageSalary()) + " PLN");

        employeeService.getEmployeeWithHighestSalary().ifPresent(emp ->
                System.out.println("Najwyższe wynagrodzenie: " + emp.getFirstName() + " " +
                        emp.getLastName() + " - " + emp.getSalary() + " PLN")
        );
        System.out.println();

        // 5. STATYSTYKI DLA KONKRETNEJ FIRMY
        System.out.println("5. STATYSTYKI DLA FIRMY: TechCorp");
        System.out.println("-".repeat(80));

        List<Employee> techCorpEmployees = employeeService.findEmployeesInCompany("TechCorp");
        System.out.println("Liczba pracowników w TechCorp: " + techCorpEmployees.size());

        if (!techCorpEmployees.isEmpty()) {
            double techCorpAvgSalary = techCorpEmployees.stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
            System.out.println("Średnie wynagrodzenie w TechCorp: " + String.format("%.2f", techCorpAvgSalary) + " PLN");

            System.out.println("\nPracownicy TechCorp:");
            techCorpEmployees.forEach(emp ->
                    System.out.println("  - " + emp.getFirstName() + " " + emp.getLastName() +
                            " (" + emp.getPosition() + ") - " + emp.getSalary() + " PLN")
            );
        }
        System.out.println();

        // 6. GRUPOWANIE PRACOWNIKÓW WEDŁUG STANOWISKA
        System.out.println("6. LICZBA PRACOWNIKÓW NA POSZCZEGÓLNYCH STANOWISKACH");
        System.out.println("-".repeat(80));

        employeeService.countEmployeesOnPositions().forEach((position, count) ->
                System.out.println("  " + position + ": " + count + " pracowników")
        );
        System.out.println();

        // 7. WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ
        System.out.println("7. WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ");
        System.out.println("-".repeat(80));

        List<Employee> inconsistentSalaries = employeeService.validateSalaryConsistency();

        if (inconsistentSalaries.isEmpty()) {
            System.out.println("Wszystkie wynagrodzenia są zgodne z bazowymi stawkami stanowisk.");
        } else {
            System.out.println("Znaleziono " + inconsistentSalaries.size() +
                    " pracowników z wynagrodzeniem poniżej bazowej stawki:");
            System.out.println();

            inconsistentSalaries.forEach(emp -> {
                double baseSalary = emp.getPosition().getBaseSalary();
                System.out.println(" - " + emp.getFirstName() + " " + emp.getLastName());
                System.out.println("Stanowisko: " + emp.getPosition());
                System.out.println("Aktualne wynagrodzenie: " + emp.getSalary() + " PLN");
                System.out.println("Bazowa stawka: " + baseSalary + " PLN");
                System.out.println();
            });
        }

        System.out.println("!!! Koniec !!!");
    }
}