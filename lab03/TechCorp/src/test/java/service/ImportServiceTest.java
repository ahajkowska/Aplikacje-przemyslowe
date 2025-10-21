package service;

import model.Employee;
import model.ImportSummary;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportServiceTest {

    private ImportService importService;
    private EmployeeService employeeService;

    @TempDir
    Path tempDir; // JUnit automatycznie tworzy i czyści katalog tymczasowy

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService(employeeService);
    }

    // POPRAWNY IMPORT

    @Nested
    @DisplayName("Import scenarios")
    class SuccessfulImportTests {

        @Test
        @DisplayName("should import single employee from valid CSV file")
        void shouldImportSingleEmployee_whenCsvIsValid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should add employee to employee service when import is successful")
        void shouldAddEmployeeToService_whenImportIsSuccessful() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(1, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should have no errors when import is successful")
        void shouldHaveNoErrors_whenImportIsSuccessful() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertTrue(summary.getErrors().isEmpty());
        }

        @Test
        @DisplayName("should import multiple employees from valid CSV file")
        void shouldImportMultipleEmployees_whenCsvIsValid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000",
                    "Piotr,Wiśniewski,piotr@firm.pl,TechCorp,PRESIDENT,25000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(3, summary.getImportedCount());
        }

        @Test
        @DisplayName("should add all employees to service when importing multiple")
        void shouldAddAllEmployeesToService_whenImportingMultiple() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(2, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should parse employee first name correctly")
        void shouldParseFirstNameCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals("Jan", employeeService.getAllEmployees().get(0).getFirstName());
        }

        @Test
        @DisplayName("should parse employee last name correctly")
        void shouldParseLastNameCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals("Kowalski", employeeService.getAllEmployees().get(0).getLastName());
        }

        @Test
        @DisplayName("should parse employee email correctly")
        void shouldParseEmailCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals("jan@firm.pl", employeeService.getAllEmployees().get(0).getEmail());
        }

        @Test
        @DisplayName("should parse employee company correctly")
        void shouldParseCompanyCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals("TechCorp", employeeService.getAllEmployees().get(0).getCompany());
        }

        @Test
        @DisplayName("should parse employee position correctly")
        void shouldParsePositionCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(Position.DEVELOPER, employeeService.getAllEmployees().get(0).getPosition());
        }

        @Test
        @DisplayName("should parse employee salary correctly")
        void shouldParseSalaryCorrectly() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(9000.0, employeeService.getAllEmployees().get(0).getSalary());
        }

        @Test
        @DisplayName("should handle position in lowercase")
        void shouldHandlePosition_whenInLowercase() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,developer,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should handle position in mixed case")
        void shouldHandlePosition_whenInMixedCase() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DeVeLoPeR,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should trim whitespace from fields")
        void shouldTrimWhitespace_whenFieldsHaveSpaces() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "  Jan  ,  Kowalski  ,  jan@firm.pl  ,  TechCorp  ,  DEVELOPER  ,  9000  "
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should skip empty lines in CSV file")
        void shouldSkipEmptyLines_whenCsvHasBlankLines() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(2, summary.getImportedCount());
        }

        @Test
        @DisplayName("should handle decimal salary values")
        void shouldHandleDecimalSalary_whenSalaryHasDecimals() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9500.50"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(9500.50, employeeService.getAllEmployees().get(0).getSalary());
        }
    }

    @Test
    void testImportOneEmployee_whenCsvContainsOneValidRow(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("one_employee.csv");
        Files.writeString(file, "firstName,lastName,email,company,position,salary\nJan,Kowalski,jan@firma.pl,FirmaX,DEVELOPER,7000\n");

        ImportSummary summary = importService.importFromCsv(file.toString());
        List<Employee> employees = employeeService.getAllEmployees();

        assertEquals(1, employees.size());
    }

    @Test
    void testNotImportEmployee_whenCsvContainsInvalidPosition(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("invalid_position.csv");
        Files.writeString(file, "firstName,lastName,email,company,position,salary\nJan,Kowalski,jan@firma.pl,FirmaX,INVALIDPOSITION,7000\n");

        ImportSummary summary = importService.importFromCsv(file.toString());
        List<Employee> employees = employeeService.getAllEmployees();

        assertEquals(0, employees.size());
    }

    @Test
    void testNotImportEmployee_whenCsvContainsNegativeSalary(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("negative_salary.csv");
        Files.writeString(file, "firstName,lastName,email,company,position,salary\nJan,Kowalski,jan@firma.pl,FirmaX,DEVELOPER,-7000\n");

        ImportSummary summary = importService.importFromCsv(file.toString());
        List<Employee> employees = employeeService.getAllEmployees();

        assertEquals(0, employees.size());
    }

    @Test
    void testReturnCorrectImportedCount_whenCsvContainsMixedRows(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("mixed_rows.csv");
        StringBuilder sb = new StringBuilder();
        sb.append("firstName,lastName,email,company,position,salary\n");
        sb.append("Jan,Kowalski,jan@firma.pl,FirmaX,DEVELOPER,7000\n"); // poprawny
        sb.append("Anna,Nowak,anna@firma.pl,FirmaX,WRONGPOSITION,8000\n"); // złe stanowisko
        sb.append("Piotr,Nowak,piotr@firma.pl,FirmaX,MANAGER,-1000\n"); // złe wynagrodzenie

        Files.writeString(file, sb.toString());

        ImportSummary summary = importService.importFromCsv(file.toString());

        assertEquals(1, summary.getImportedCount());
    }

    @Test
    void testNotImportAnyEmployees_whenCsvIsEmpty(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.writeString(file, "");

        ImportSummary summary = importService.importFromCsv(file.toString());
        List<Employee> employees = employeeService.getAllEmployees();

        assertEquals(0, employees.size());
    }

    // OBSŁUGA BŁĘDÓW - NIEPOPRAWNE STANOWISKO

    @Nested
    @DisplayName("Invalid position - should continue import")
    class InvalidPositionTests {

        @Test
        @DisplayName("should add error when position is invalid")
        void shouldAddError_whenPositionIsInvalid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should not import employee when position is invalid")
        void shouldNotImportEmployee_whenPositionIsInvalid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }

        @Test
        @DisplayName("should not add employee to service when position is invalid")
        void shouldNotAddEmployeeToService_whenPositionIsInvalid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000"
            );

            importService.importFromCsv(csvFile.toString());

            assertTrue(employeeService.getAllEmployees().isEmpty());
        }

        @Test
        @DisplayName("should continue import after invalid position error")
        void shouldContinueImport_afterInvalidPositionError() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should import valid employee after invalid position error")
        void shouldImportValidEmployee_afterInvalidPositionError() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(1, employeeService.getAllEmployees().size());
        }

        @Test
        @DisplayName("should have one error when one position is invalid")
        void shouldHaveOneError_whenOnePositionIsInvalid() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID_POSITION,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }
    }

    // OBSŁUGA BŁĘDÓW - NIEPOPRAWNE WYNAGRODZENIE

    @Nested
    @DisplayName("Invalid salary handling - should continue import")
    class InvalidSalaryTests {

        @Test
        @DisplayName("should add error when salary is negative")
        void shouldAddError_whenSalaryIsNegative() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,-1000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should not import employee when salary is negative")
        void shouldNotImportEmployee_whenSalaryIsNegative() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,-1000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }

        @Test
        @DisplayName("should add error when salary is not a number")
        void shouldAddError_whenSalaryIsNotANumber() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,not_a_number"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should not import employee when salary is not a number")
        void shouldNotImportEmployee_whenSalaryIsNotANumber() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,not_a_number"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }

        @Test
        @DisplayName("should continue import after negative salary error")
        void shouldContinueImport_afterNegativeSalaryError() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,-1000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getImportedCount());
        }

        @Test
        @DisplayName("should import valid employee after salary error")
        void shouldImportValidEmployee_afterSalaryError() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,-1000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            importService.importFromCsv(csvFile.toString());

            assertEquals(1, employeeService.getAllEmployees().size());
        }
    }

    // OBSŁUGA BŁĘDÓW - STRUKTURA PLIKU

    @Nested
    @DisplayName("File structure errors - should continue import")
    class FileStructureErrorsTests {

        @Test
        @DisplayName("should add error when line has too few fields")
        void shouldAddError_whenLineHasTooFewFields() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should not import employee when line has too few fields")
        void shouldNotImportEmployee_whenLineHasTooFewFields() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }

        @Test
        @DisplayName("should add error when line has too many fields")
        void shouldAddError_whenLineHasTooManyFields() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000,ExtraField"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should not import employee when line has too many fields")
        void shouldNotImportEmployee_whenLineHasTooManyFields() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000,ExtraField"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }
    }

    // PODSUMOWANIE IMPORTU

    @Nested
    @DisplayName("ImportSummary verification")
    class ImportSummaryTests {

        @Test
        @DisplayName("should have correct imported count in summary")
        void shouldHaveCorrectImportedCount_inSummary() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(2, summary.getImportedCount());
        }

        @Test
        @DisplayName("should have correct error count in summary")
        void shouldHaveCorrectErrorCount_inSummary() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,-1000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(2, summary.getErrors().size());
        }

        @Test
        @DisplayName("should have zero imported count when all records fail")
        void shouldHaveZeroImportedCount_whenAllRecordsFail() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,INVALID,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,MANAGER,-1000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(0, summary.getImportedCount());
        }

        @Test
        @DisplayName("should have mixed counts when some records succeed and some fail")
        void shouldHaveMixedCounts_whenSomeSucceedAndSomeFail() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,INVALID,12000",
                    "Piotr,Wiśniewski,piotr@firm.pl,TechCorp,PRESIDENT,25000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(2, summary.getImportedCount());
        }

        @Test
        @DisplayName("should have correct error count when some records succeed and some fail")
        void shouldHaveCorrectErrorCount_whenSomeSucceedAndSomeFail() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,INVALID,12000",
                    "Piotr,Wiśniewski,piotr@firm.pl,TechCorp,PRESIDENT,25000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertEquals(1, summary.getErrors().size());
        }

        @Test
        @DisplayName("should have correct line number in error message")
        void shouldHaveCorrectLineNumber_inErrorMessage() throws IOException {
            Path csvFile = createCsvFile(
                    "FirstName,LastName,Email,Company,Position,Salary",
                    "Jan,Kowalski,jan@firm.pl,TechCorp,DEVELOPER,9000",
                    "Anna,Nowak,anna@firm.pl,DataCorp,INVALID,12000"
            );

            ImportSummary summary = importService.importFromCsv(csvFile.toString());

            assertTrue(summary.getErrors().get(0).startsWith("Line 3:"));
        }
    }

    // METODA POMOCNICZA

    /*
     Tworzy tymczasowy plik CSV z podanymi liniami.
     */
    private Path createCsvFile(String... lines) throws IOException {
        Path csvFile = tempDir.resolve("test_" + System.nanoTime() + ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.toFile()))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine(); // Automatycznie używa separatora linii dla danego OS
            }
        }

        return csvFile;
    }
}