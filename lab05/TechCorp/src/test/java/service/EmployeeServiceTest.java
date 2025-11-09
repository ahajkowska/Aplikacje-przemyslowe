package service;

import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.service.EmployeeService;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Stream;

class EmployeeServiceTest {
    EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }

    // DODAWANIE PRACOWNIKA

    @Nested
    @DisplayName("addEmployee() tests")
    class AddEmployeeTests {

        @Test
        @DisplayName("should return true when employee is added successfully")
        void shouldReturnTrue_whenEmployeeIsAddedSuccessfully() {
            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);

            boolean result = employeeService.addEmployee(employee);

            assertTrue(result);
        }

        @Test
        @DisplayName("should increase employee count when employee is added")
        void shouldIncreaseEmployeeCount_whenEmployeeIsAdded() {
            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);

            employeeService.addEmployee(employee);

            assertEquals(1, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should contain added employee in the list")
        void shouldContainAddedEmployee_whenEmployeeIsAdded() {
            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);

            employeeService.addEmployee(employee);

            assertTrue(employeeService.getAllEmployees().contains(employee));
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when employee is null")
        void shouldThrowException_whenEmployeeIsNull() {
            assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(null));
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email already exists")
        void shouldThrowException_whenEmailAlreadyExists() {
            Employee firstEmployee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(firstEmployee);

            Employee duplicateEmail = new Employee("Anna", "Nowak", "jan@firm.pl", "DataCorp", Position.MANAGER, 12000);

            assertThrows(DuplicateEmailException.class, () -> employeeService.addEmployee(duplicateEmail));
        }

        @Test
        @DisplayName("should add multiple employees when all have unique emails")
        void shouldAddMultipleEmployees_whenAllHaveUniqueEmails() {
            Employee employee1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            Employee employee2 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            Employee employee3 = new Employee("Adam", "Małysz", "adam@firm.pl", "Corp", Position.DEVELOPER, 8500);


            employeeService.addEmployee(employee1);
            employeeService.addEmployee(employee2);
            employeeService.addEmployee(employee3);

            assertEquals(3, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should not increase employee count when email already exists")
        void shouldNotIncreaseEmployeeCount_whenEmailAlreadyExists() {
            Employee firstEmployee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(firstEmployee);

            Employee duplicateEmail = new Employee("Anna", "Nowak", "JAN@FIRM.PL", "DataCorp", Position.MANAGER, 12000);

            assertThrows(
                    com.techcorp.employee.exception.DuplicateEmailException.class,
                    () -> employeeService.addEmployee(duplicateEmail)
            );

            assertEquals(1, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should not allow adding employee with null firstName")
        void shouldNotAllowAddingEmployee_withNullFirstName() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> employeeService.addEmployee(
                            new Employee(null, "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000)
                    )
            );
        }

        @Test
        @DisplayName("should contain only first employee when duplicate email is rejected")
        void shouldContainOnlyFirstEmployee_whenDuplicateEmailIsRejected() {
            Employee firstEmployee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(firstEmployee);

            Employee duplicateEmail = new Employee("Anna", "Nowak", "jan@firm.pl", "DataCorp", Position.MANAGER, 12000);

            assertThrows(
                    com.techcorp.employee.exception.DuplicateEmailException.class,
                    () -> employeeService.addEmployee(duplicateEmail)
            );

            assertTrue(employeeService.getAllEmployees().contains(firstEmployee));
        }
    }

    // WALIDACJA EMPLOYEE

    @Nested
    @DisplayName("Employee constructor validation tests")
    class EmployeeConstructorValidationTests {

        @Nested
        @DisplayName("Null field validation")
        class NullFieldTests {

            @ParameterizedTest(name = "should throw exception when {0} is null")
            @MethodSource("provideNullFieldScenarios")
            @DisplayName("should throw exception when any required field is null")
            void shouldThrowException_whenRequiredFieldIsNull(
                    String fieldName,
                    String firstName,
                    String lastName,
                    String email,
                    String company,
                    Position position) {

                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Employee(firstName, lastName, email, company, position, 9000)
                );
            }

            static Stream<Arguments> provideNullFieldScenarios() {
                return Stream.of(
                        Arguments.of("firstName", null, "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER),
                        Arguments.of("lastName", "Jan", null, "jan@firm.pl", "Corp", Position.DEVELOPER),
                        Arguments.of("email", "Jan", "Kowalski", null, "Corp", Position.DEVELOPER),
                        Arguments.of("company", "Jan", "Kowalski", "jan@firm.pl", null, Position.DEVELOPER),
                        Arguments.of("position", "Jan", "Kowalski", "jan@firm.pl", "Corp", null)
                );
            }
        }

        @Test
        @DisplayName("should throw exception when salary is negative")
        void shouldThrowException_whenSalaryIsNegative() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, -100)
            );
        }

        @Test
        @DisplayName("should create employee when salary is positive")
        void shouldCreateEmployee_whenSalaryIsPositive() {

            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 5000);

            assertNotNull(employee);
        }

        @Test
        @DisplayName("should set salary to 5000 when employee is created with 5000 salary")
        void shouldSetSalaryTo5000_whenEmployeeIsCreatedWith5000Salary() {

            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 5000);

            assertEquals(5000, employee.getSalary());
        }
    }

    // POBIERANIE WSZYSTKICH PRACOWNIKÓW

    @Nested
    @DisplayName("getAllEmployees() tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyList_whenNoEmployeesExist() {
            List<Employee> employees = employeeService.getAllEmployees();

            assertTrue(employees.isEmpty());
        }

        @Test
        @DisplayName("should return correct count when employees are added")
        void shouldReturnCorrectCount_whenEmployeesAreAdded() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> employees = employeeService.getAllEmployees();

            assertEquals(2, employees.size());
        }

        @Test
        @DisplayName("should contain first added employee")
        void shouldContainFirstAddedEmployee() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> employees = employeeService.getAllEmployees();

            assertTrue(employees.contains(emp1));
        }

        @Test
        @DisplayName("should contain second added employee")
        void shouldContainSecondAddedEmployee() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> employees = employeeService.getAllEmployees();

            assertTrue(employees.contains(emp2));
        }
    }

    // WYSZUKIWANIE PO FIRMIE

    @Nested
    @DisplayName("findEmployeesInCompany() tests")
    class FindEmployeesInCompanyTests {

        @Test
        @DisplayName("should return correct count when company exists")
        void shouldReturnCorrectCount_whenCompanyExists() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "TechCorp", Position.MANAGER, 12000);
            Employee emp3 = new Employee("Piotr", "Wiśniewski", "piotr@firm.pl", "DataCorp", Position.DEVELOPER, 8500);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);
            employeeService.addEmployee(emp3);

            List<Employee> result = employeeService.findEmployeesInCompany("TechCorp");

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should contain employee from specified company")
        void shouldContainEmployee_whenEmployeeIsFromSpecifiedCompany() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "TechCorp", Position.MANAGER, 12000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> result = employeeService.findEmployeesInCompany("TechCorp");

            assertTrue(result.contains(emp1));
        }

        @Test
        @DisplayName("should not contain employee from different company")
        void shouldNotContainEmployee_whenEmployeeIsFromDifferentCompany() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Piotr", "Wiśniewski", "piotr@firm.pl", "DataCorp", Position.DEVELOPER, 8500);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> result = employeeService.findEmployeesInCompany("TechCorp");

            assertFalse(result.contains(emp2));
        }

        @Test
        @DisplayName("should return empty list when company does not exist")
        void shouldReturnEmptyList_whenCompanyDoesNotExist() {
            Employee employee = new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(employee);

            List<Employee> result = employeeService.findEmployeesInCompany("NonExistentCorp");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyList_whenNoEmployeesExist() {
            List<Employee> result = employeeService.findEmployeesInCompany("TechCorp");

            assertTrue(result.isEmpty());
        }

        @ParameterizedTest(name = "should throw exception when company name is ''{0}''")
        @NullAndEmptySource // null, ""
        @ValueSource(strings = {"   ", "  ", "\t", "\n"})
        @DisplayName("should throw exception when company name is null, empty or blank")
        void shouldThrowException_whenCompanyNameIsInvalid(String invalidCompanyName) {
            // when & then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> employeeService.findEmployeesInCompany(invalidCompanyName)
            );
        }
    }

    // SORTOWANIE WEDŁUG NAZWISKA

    @Nested
    @DisplayName("getEmployeesSortedByLastName() tests")
    class SortByLastNameTests {

        @Test
        @DisplayName("should return correct count when employees are sorted")
        void shouldReturnCorrectCount_whenEmployeesAreSorted() {
            Employee emp1 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            Employee emp2 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            List<Employee> sorted = employeeService.getEmployeesSortedByLastName();

            assertEquals(2, sorted.size());
        }

        @Test
        @DisplayName("should place Kowalski before Nowak alphabetically")
        void shouldPlaceKowalskiBeforeNowak_whenSortingAlphabetically() {
            Employee nowak = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            Employee kowalski = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(nowak);
            employeeService.addEmployee(kowalski);

            List<Employee> sorted = employeeService.getEmployeesSortedByLastName();

            assertEquals("Kowalski", sorted.get(0).getLastName());
        }

        @Test
        @DisplayName("should place Nowak after Kowalski alphabetically")
        void shouldPlaceNowakAfterKowalski_whenSortingAlphabetically() {
            Employee nowak = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000);
            Employee kowalski = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(nowak);
            employeeService.addEmployee(kowalski);

            List<Employee> sorted = employeeService.getEmployeesSortedByLastName();

            assertEquals("Nowak", sorted.get(1).getLastName());
        }

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyList_whenNoEmployeesExist() {
            List<Employee> sorted = employeeService.getEmployeesSortedByLastName();

            assertTrue(sorted.isEmpty());
        }
    }

    // LICZENIE PRACOWNIKÓW NA STANOWISKACH

    @Nested
    @DisplayName("countEmployeesOnPositions() tests")
    class CountEmployeesTests {

        @Test
        @DisplayName("should return correct number of position groups")
        void shouldReturnCorrectNumberOfGroups_whenCountingEmployees() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000));

            Map<Position, Long> counts = employeeService.countEmployeesOnPositions();

            assertEquals(2, counts.size());
        }

        @Test
        @DisplayName("should count developers correctly")
        void shouldCountDevelopersCorrectly() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@firm.pl", "Corp", Position.DEVELOPER, 8500));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000));

            Map<Position, Long> counts = employeeService.countEmployeesOnPositions();

            assertEquals(2L, counts.get(Position.DEVELOPER));
        }

        @Test
        @DisplayName("should count managers correctly")
        void shouldCountManagersCorrectly() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000));

            Map<Position, Long> counts = employeeService.countEmployeesOnPositions();

            assertEquals(1L, counts.get(Position.MANAGER));
        }

        @Test
        @DisplayName("should return empty map when no employees exist")
        void shouldReturnEmptyMap_whenNoEmployeesExist() {
            Map<Position, Long> counts = employeeService.countEmployeesOnPositions();

            assertTrue(counts.isEmpty());
        }
    }

    // ŚREDNIE WYNAGRODZENIE

    @Nested
    @DisplayName("averageSalary() tests")
    class AverageSalaryTests {

        @Test
        @DisplayName("should calculate correct average for multiple employees")
        void shouldCalculateCorrectAverage_whenMultipleEmployeesExist() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000));
            employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@firm.pl", "Corp", Position.DEVELOPER, 6000));

            double average = employeeService.averageSalary();

            assertEquals(9000.0, average, 0.01);
        }

        @Test
        @DisplayName("should return zero when no employees exist")
        void shouldReturnZero_whenNoEmployeesExist() {
            double average = employeeService.averageSalary();

            assertEquals(0.0, average);
        }

        @Test
        @DisplayName("should return employee salary when only one employee exists")
        void shouldReturnEmployeeSalary_whenOnlyOneEmployeeExists() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));

            double average = employeeService.averageSalary();

            assertEquals(9000.0, average, 0.01);
        }

        @Test
        @DisplayName("should include employee with zero salary in average calculation")
        void shouldIncludeZeroSalary_inAverageCalculation() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 0));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.DEVELOPER, 6000));

            double average = employeeService.averageSalary();

            assertEquals(3000.0, average, 0.01);
        }
    }

    // NAJWYŻSZE WYNAGRODZENIE
    @Nested
    @DisplayName("getEmployeeWithHighestSalary() tests")
    class HighestSalaryTests {

        @Test
        @DisplayName("should return employee with the highest salary")
        void shouldReturnEmployeeWithHighestSalary() {
            Employee emp1 = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000);
            Employee emp2 = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.PRESIDENT, 25000);
            employeeService.addEmployee(emp1);
            employeeService.addEmployee(emp2);

            Optional<Employee> highest = employeeService.getEmployeeWithHighestSalary();

            assertEquals(emp2, highest.get());
        }

        @Test
        @DisplayName("should return correct salary value for highest paid employee")
        void shouldReturnCorrectSalaryValue_forHighestPaidEmployee() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.PRESIDENT, 25000));

            Optional<Employee> highest = employeeService.getEmployeeWithHighestSalary();

            assertEquals(25000, highest.get().getSalary());
        }

        @Test
        @DisplayName("should return empty Optional when no employees exist")
        void shouldReturnEmptyOptional_whenNoEmployeesExist() {
            Optional<Employee> highest = employeeService.getEmployeeWithHighestSalary();

            assertTrue(highest.isEmpty());
        }
    }

    // WALIDACJA SPÓJNOŚCI WYNAGRODZEŃ

    @Nested
    @DisplayName("validateSalaryConsistency() tests")
    class ValidateSalaryConsistencyTests {

        @Test
        @DisplayName("should return correct count of employees with salaries lower than its base")
        void shouldReturnCorrectCount_whenEmployeesHaveLowerSalariesThanItsBase() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 5000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.DEVELOPER, 7000));
            employeeService.addEmployee(new Employee("Adam", "Małysz", "adam@firm.pl", "Corp", Position.PRESIDENT, 35000));

            List<Employee> lower = employeeService.validateSalaryConsistency();

            assertEquals(2, lower.size());
        }

        @Test
        @DisplayName("should contain employee with salary below base")
        void shouldContainEmployee_whenSalaryIsBelowBase() {
            Employee belowBase = new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 5000);
            employeeService.addEmployee(belowBase);

            List<Employee> invalid = employeeService.validateSalaryConsistency();

            assertTrue(invalid.contains(belowBase));
        }

        @Test
        @DisplayName("should not contain employee with salary above base")
        void shouldNotContainEmployee_whenSalaryIsAboveBase() {
            Employee aboveBase = new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.DEVELOPER, 9000);
            employeeService.addEmployee(aboveBase);

            List<Employee> invalid = employeeService.validateSalaryConsistency();

            assertFalse(invalid.contains(aboveBase));
        }

        @Test
        @DisplayName("should return empty list when all salaries are valid")
        void shouldReturnEmptyList_whenAllSalariesAreValid() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "Corp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "Corp", Position.MANAGER, 12000));

            List<Employee> invalid = employeeService.validateSalaryConsistency();

            assertTrue(invalid.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyList_whenNoEmployeesExist() {
            List<Employee> invalid = employeeService.validateSalaryConsistency();

            assertTrue(invalid.isEmpty());
        }
    }

    // STATYSTYKI FIRMOWE

    @Nested
    @DisplayName("getCompanyStatistics() tests")
    class CompanyStatisticsTests {

        @Test
        @DisplayName("should return correct number of company groups")
        void shouldReturnCorrectNumberOfCompanies() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "DataCorp", Position.MANAGER, 12000));

            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertEquals(2, stats.size());
        }

        @Test
        @DisplayName("should return empty map when no employees exist")
        void shouldReturnEmptyMap_whenNoEmployeesExist() {
            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertTrue(stats.isEmpty());
        }

        @Test
        @DisplayName("should calculate correct employee count for company")
        void shouldCalculateCorrectEmployeeCount_forCompany() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "TechCorp", Position.MANAGER, 12000));

            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertEquals(2, stats.get("TechCorp").getEmployeeCount());
        }

        @Test
        @DisplayName("should calculate correct average salary for company")
        void shouldCalculateCorrectAverageSalary_forCompany() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "TechCorp", Position.MANAGER, 12000));

            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertEquals(10500.0, stats.get("TechCorp").getAverageSalary(), 0.01);
        }

        @Test
        @DisplayName("should identify correct highest paid employee name")
        void shouldIdentifyCorrectHighestPaidEmployeeName() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000));
            employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@firm.pl", "TechCorp", Position.MANAGER, 12000));

            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertEquals("Anna Nowak", stats.get("TechCorp").getHighestPaidEmployee());
        }

        @Test
        @DisplayName("should format highest paid employee name with space between first and last name")
        void shouldFormatNameCorrectly_forHighestPaidEmployee() {
            employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@firm.pl", "TechCorp", Position.DEVELOPER, 9000));

            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

            assertTrue(stats.get("TechCorp").getHighestPaidEmployee().contains(" "));
        }
    }
}