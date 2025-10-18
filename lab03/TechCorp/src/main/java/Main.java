import exception.ApiException;
import model.CompanyStatistics;
import model.Employee;
import model.ImportSummary;
import model.Position;
import service.ApiService;
import service.EmployeeService;
import service.ImportService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        EmployeeService system = new EmployeeService();

        // 1. Dodawanie pracowników ręcznie
        system.addEmployee(new Employee("Adam", "Małysz", "adam.malysz@gmail.com", "TechCorp", Position.PRESIDENT, 30000));
        system.addEmployee(new Employee("Harry", "Potter", "harry.potter@techcorp.eu", "TechCorp", Position.DEVELOPER, 15000));
        system.addEmployee(new Employee("Magda", "Gessler", "magda.gessler@gmail.com", "MiniCorp", Position.INTERN, 3000));

        // test duplikatu
        boolean added = system.addEmployee(new Employee("Duplicate", "Email", "harry.potter@techcorp.eu",
                "TechCorp", Position.DEVELOPER, 8000));
        System.out.println("Employee with duplicate email added: " + added);

        System.out.println("\n--- All Employees ---");
        system.getAllEmployees().forEach(System.out::println);

        // 2. Import z pliku CSV
        System.out.println("\n--- Import from CSV ---");
        ImportService importService = new ImportService(system);

        String csvFilePath = "employees.csv";
        createSampleCsvFile(csvFilePath); // utwórz przykładowy plik jeśli nie istnieje

        ImportSummary summary = importService.importFromCsv(csvFilePath);
        System.out.println("Imported: " + summary.getImportedCount());
        if (!summary.getErrors().isEmpty()) {
            System.out.println("Import errors:");
            summary.getErrors().forEach(error -> System.out.println("  * " + error));
        }

        // 3. Pobieranie pracowników z API
        System.out.println("\n--- Fetching employees from API ---");
        ApiService apiService = new ApiService("https://jsonplaceholder.typicode.com/users");
        try {
            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi();
            int addedFromApi = 0;
            for (Employee e : apiEmployees) {
                if (system.addEmployee(e)) addedFromApi++;
            }
            System.out.println("Fetched from API: " + apiEmployees.size());
            System.out.println("Added unique from API: " + addedFromApi);
        } catch (ApiException e) {
            System.out.println("API error: " + e.getMessage());
        }

        // 4. Analizy i statystyki
        System.out.println("\n--- All Employees after imports ---");
        system.getAllEmployees().forEach(System.out::println);

        // Pracownicy w firmie
        String chosenCompany = "TechCorp";
        System.out.println("\n--- Employees working at " + chosenCompany + " ---");
        system.findEmployeesInCompany(chosenCompany).forEach(System.out::println);

        // Sortowanie po nazwisku
        System.out.println("\n--- Employees sorted by last name ---");
        system.getEmployeesSortedByLastName().forEach(
                e -> System.out.println(e.getLastName() + " " + e.getFirstName())
        );

        // Grupowanie po stanowisku
        System.out.println("\n--- Employees grouped by position ---");
        system.getEmployeesGroupedByPosition().forEach((position, empList) -> {
            System.out.println(position + ": ");
            empList.forEach(e -> System.out.println("  - " + e.getFirstName() + " " + e.getLastName()));
        });

        // Liczba po stanowisku
        System.out.println("\n--- Number of employees by position ---");
        system.countEmployeesOnPositions().forEach((position, count) -> {
            System.out.println(position + ": " + count);
        });

        // Średnia pensja
        System.out.println("\n--- Average salary ---");
        System.out.println("Average salary: " + system.averageSalary());

        // Najlepiej wynagradzany
        System.out.println("\n--- Employee with highest salary ---");
        system.getEmployeeWithHighestSalary().ifPresent(e ->
                System.out.println(e.getFirstName() + " " + e.getLastName() + ": " + e.getSalary()));

        // Walidacja spójności pensji
        System.out.println("\n--- Employees with salary below base of their position ---");
        List<Employee> inconsistent = system.validateSalaryConsistency();
        if (inconsistent.isEmpty()) {
            System.out.println("All employees have salary equal or above base for position.");
        } else {
            inconsistent.forEach(e ->
                    System.out.println(e.getFirstName() + " " + e.getLastName() +
                            ": " + e.getSalary() + " (base: " + e.getPosition().getBaseSalary() + ")"));
        }

        // Statystyki firm
        System.out.println("\n--- Company statistics ---");
        Map<String, CompanyStatistics> stats = system.getCompanyStatistics();
        stats.forEach((company, stat) -> System.out.println(company + ": " + stat));
    }

    // Tworzy przykładowy plik CSV jeśli nie istnieje
    private static void createSampleCsvFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("firstName,lastName,email,company,position,salary");
            writer.println("Harry,Potter,harrypotter@dataCorp.com,DataCorp,MANAGER,12500");
            writer.println("Hermiona,Granger,hermiona.granger@techcorp.com,TechCorp,VICE_PRESIDENT,18500");
            writer.println("Adam,Małysz,adam.malysz@othercorp.com,OtherCorp,INTERN,2500");  // poniżej minimum
            writer.println("Błędny,Wiersz,blad@firma.com,Firma,NIEISTNIEJACE_STANOWISKO,5000");  // błędne stanowisko
            writer.println("Inny,Błąd,inny.blad@firma.com,Firma,DEVELOPER,-1000");  // ujemna pensja
            writer.println("Za,Mało,Kolumn,OtherCorp,DEVELOPER");  // za mało kolumn
        } catch (IOException e) {
            System.err.println("Błąd podczas tworzenia pliku CSV: " + e.getMessage());
        }
    }
}