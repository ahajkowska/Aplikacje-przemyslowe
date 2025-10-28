package com.techcorp.employee;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.service.ApiService;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmployeeManagementApplication.class);

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final List<Employee> xmlEmployees;
    private final String csvFileProperty;

    public EmployeeManagementApplication(
            EmployeeService employeeService,
            ImportService importService,
            ApiService apiService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-file}") String csvFileProperty) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.apiService = apiService;
        this.xmlEmployees = xmlEmployees;
        this.csvFileProperty = csvFileProperty;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("EMPLOYEE MANAGEMENT APP");
        log.info("-".repeat(80));

        // 1. IMPORT PRACOWNIKÓW Z PLIKU CSV
        log.info("1. IMPORT PRACOWNIKÓW Z PLIKU CSV");
        log.info("-".repeat(80));

        ImportSummary csvImportSummary = importCsvFromClasspath();

        log.info("Zaimportowano: {} pracowników", csvImportSummary.getImportedCount());
        if (!csvImportSummary.getErrors().isEmpty()) {
            log.warn("Błędy podczas importu:");
            csvImportSummary.getErrors().forEach(log::warn);
        }

        // 2. DODANIE PRACOWNIKÓW Z BEANA XML
        log.info("2. DODANIE PRACOWNIKÓW Z KONFIGURACJI XML");
        log.info("-".repeat(80));

        int xmlImportedCount = 0;
        int xmlErrorsCount = 0;

        for (Employee employee : xmlEmployees) {
            try {
                boolean added = employeeService.addEmployee(employee);
                if (added) {
                    xmlImportedCount++;
                    log.info("Dodano: {} {} ({})", employee.getFirstName(), employee.getLastName(), employee.getEmail());
                }
            } catch (IllegalArgumentException e) {
                xmlErrorsCount++;
                log.warn("Błąd dla: " + employee.getEmail() + " - " + e.getMessage());
            }
        }

        log.info("Podsumowanie importu z XML: Zaimportowano: {} pracowników, Błędy: {}", xmlImportedCount, xmlErrorsCount);

        // 3. POBRANIE DANYCH Z REST API
        log.info("3. POBIERANIE PRACOWNIKÓW Z REST API");
        log.info("-".repeat(80));

        try {
            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi();
            int apiImportedCount = 0;

            for (Employee employee : apiEmployees) {
                try {
                    boolean added = employeeService.addEmployee(employee);
                    if (added) {
                        apiImportedCount++;
                    }
                } catch (IllegalArgumentException e) { /* duplikat email */ }
            }

            log.info("Pobrano z API: {} pracowników", apiEmployees.size());
            log.info("Dodano do systemu: {} nowych pracowników", apiImportedCount);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania danych z API: {}", e.getMessage(), e);
        }
        log.info("");

        // 4. WYŚWIETLENIE OGÓLNYCH STATYSTYK
        log.info("4. OGÓLNE STATYSTYKI SYSTEMU");
        log.info("-".repeat(80));

        List<Employee> allEmployees = employeeService.getAllEmployees();
        log.info("Łączna liczba pracowników w systemie: {}", allEmployees.size());
        log.info("Średnie wynagrodzenie: {} PLN", String.format("%.2f", employeeService.averageSalary()));
        employeeService.getEmployeeWithHighestSalary().ifPresent(emp ->
                log.info("Najwyższe wynagrodzenie: {} {} - {} PLN", emp.getFirstName(), emp.getLastName(), emp.getSalary())
        );
        log.info("");

        // 5. STATYSTYKI DLA KONKRETNEJ FIRMY
        log.info("5. STATYSTYKI DLA FIRMY: TechCorp");
        log.info("-".repeat(80));
        List<Employee> techCorpEmployees = employeeService.findEmployeesInCompany("TechCorp");
        log.info("Liczba pracowników w TechCorp: {}", techCorpEmployees.size());
        if (!techCorpEmployees.isEmpty()) {
            double techCorpAvgSalary = techCorpEmployees.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
            log.info("Średnie wynagrodzenie w TechCorp: {} PLN", String.format("%.2f", techCorpAvgSalary));
            techCorpEmployees.forEach(emp ->
                    log.info("  - {} {} ({}) - {} PLN", emp.getFirstName(), emp.getLastName(), emp.getPosition(), emp.getSalary())
            );
        }

        // 6. GRUPOWANIE PRACOWNIKÓW WEDŁUG STANOWISKA
        log.info("6. LICZBA PRACOWNIKÓW NA POSZCZEGÓLNYCH STANOWISKACH");
        log.info("-".repeat(80));
        employeeService.countEmployeesOnPositions().forEach((position, count) ->
                log.info("  {}: {} pracowników", position, count)
        );

        // 7. WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ
        log.info("7. WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ");
        log.info("-".repeat(80));

        List<Employee> inconsistentSalaries = employeeService.validateSalaryConsistency();
        if (inconsistentSalaries.isEmpty()) {
            log.info("Wszystkie wynagrodzenia są zgodne z bazowymi stawkami stanowisk.");
        } else {
            log.warn("Znaleziono {} pracowników z wynagrodzeniem poniżej bazowej stawki:", inconsistentSalaries.size());
            inconsistentSalaries.forEach(emp -> {
                double baseSalary = emp.getPosition().getBaseSalary();
                log.warn("{} {}", emp.getFirstName(), emp.getLastName());
                log.warn("- Stanowisko: {}", emp.getPosition());
                log.warn("- Aktualne wynagrodzenie: {} PLN", emp.getSalary());
                log.warn("- Bazowa stawka: {} PLN", baseSalary);
            });
        }

        log.info("!!! Koniec !!!");
    }

    private ImportSummary importCsvFromClasspath() {
        ClassPathResource resource = new ClassPathResource(csvFileProperty);
        Path tempFile = null;

        try (InputStream is = resource.getInputStream()) {
            tempFile = Files.createTempFile("employees-", ".csv");
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Przetwarzanie pliku: {}", csvFileProperty);
            return importService.importFromCsv(tempFile.toString());
        } catch (Exception e) {
            log.error("Błąd podczas importu '{}': {}", csvFileProperty, e.getMessage(), e);
            ImportSummary errorSummary = new ImportSummary();
            errorSummary.addError(0, "Błąd podczas wczytywania: " + e.getMessage());
            return errorSummary;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    log.debug("Nie udało się usunąć pliku tymczasowego", ex);
                }
            }
        }
    }
}

