package com.techcorp.employee.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Serwis do generowania raportów w różnych formatach (CSV, PDF).
 */
@Service
public class ReportGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ReportGeneratorService.class);

    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;

    public ReportGeneratorService(EmployeeService employeeService, FileStorageService fileStorageService) {
        this.employeeService = employeeService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Generuje raport CSV ze wszystkimi pracownikami.
     *
     * @return tablica bajtów reprezentująca plik CSV
     */
    public byte[] generateAllEmployeesCsvReport() {
        List<Employee> employees = employeeService.getAllEmployees();
        return generateCsvContent(employees);
    }

    /**
     * Generuje raport CSV dla wybranej firmy.
     *
     * @param companyName nazwa firmy
     * @return tablica bajtów reprezentująca plik CSV
     */
    public byte[] generateCompanyCsvReport(String companyName) {
        List<Employee> employees = employeeService.findEmployeesInCompany(companyName);
        return generateCsvContent(employees);
    }

    /**
     * Generuje zawartość CSV z listy pracowników.
     */
    private byte[] generateCsvContent(List<Employee> employees) {
        StringBuilder csv = new StringBuilder();
        
        // Nagłówek
        csv.append("First Name,Last Name,Email,Company,Position,Salary\n");
        
        // Dane
        for (Employee emp : employees) {
            csv.append(escapeCsv(emp.getFirstName())).append(",");
            csv.append(escapeCsv(emp.getLastName())).append(",");
            csv.append(escapeCsv(emp.getEmail())).append(",");
            csv.append(escapeCsv(emp.getCompany())).append(",");
            csv.append(emp.getPosition()).append(",");
            csv.append(emp.getSalary()).append("\n");
        }
        
        log.info("Wygenerowano raport CSV z {} pracownikami", employees.size());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Eskejpuje wartości CSV (dodaje cudzysłowy jeśli zawiera przecinek lub cudzysłów).
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Generuje raport PDF ze statystykami firmy.
     *
     * @param companyName nazwa firmy
     * @return tablica bajtów reprezentująca plik PDF
     * @throws IOException jeśli wystąpi błąd podczas generowania PDF
     */
    public byte[] generateCompanyStatisticsPdfReport(String companyName) throws IOException {
        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();
        CompanyStatistics stats = allStats.get(companyName);
        
        if (stats == null) {
            throw new IllegalArgumentException("Firma nie istnieje: " + companyName);
        }

        List<Employee> companyEmployees = employeeService.findEmployeesInCompany(companyName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Tytuł
            Paragraph title = new Paragraph("Raport statystyk firmy: " + companyName)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Sekcja: Podsumowanie
            document.add(new Paragraph("Podsumowanie").setFontSize(16).setBold());
            document.add(new Paragraph("Liczba pracowników: " + stats.getEmployeeCount()));
            document.add(new Paragraph(String.format("Średnie wynagrodzenie: %.2f PLN", stats.getAverageSalary())));
            
            String highestPaid = stats.getHighestPaidEmployee();
            if (highestPaid != null && !highestPaid.isEmpty()) {
                document.add(new Paragraph("Najwyżej opłacany: " + highestPaid));
            }

            document.add(new Paragraph("\n"));

            // Sekcja: Lista pracowników
            document.add(new Paragraph("Lista pracowników").setFontSize(16).setBold());
            
            // Tabela pracowników
            float[] columnWidths = {3, 3, 5, 3, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Nagłówki tabeli
            table.addHeaderCell("Imię");
            table.addHeaderCell("Nazwisko");
            table.addHeaderCell("Email");
            table.addHeaderCell("Stanowisko");
            table.addHeaderCell("Wynagrodzenie");

            // Wiersze z danymi
            for (Employee emp : companyEmployees) {
                table.addCell(emp.getFirstName());
                table.addCell(emp.getLastName());
                table.addCell(emp.getEmail());
                table.addCell(emp.getPosition().toString());
                table.addCell(String.format("%.2f PLN", emp.getSalary()));
            }

            document.add(table);

            // Sekcja: Statystyki według stanowisk
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Pracownicy według stanowisk").setFontSize(16).setBold());
            
            Map<Position, Long> positionCounts = employeeService.countEmployeesOnPositions();
            for (Map.Entry<Position, Long> entry : positionCounts.entrySet()) {
                document.add(new Paragraph(String.format("%s: %d pracowników", 
                        entry.getKey().toString(), entry.getValue())));
            }

            document.close();
            log.info("Wygenerowano raport PDF dla firmy: {}", companyName);
            
        } catch (Exception e) {
            log.error("Błąd podczas generowania raportu PDF: {}", e.getMessage(), e);
            throw new IOException("Nie można wygenerować raportu PDF", e);
        }

        return baos.toByteArray();
    }

    /**
     * Zapisuje raport CSV do katalogu raportów i zwraca nazwę pliku.
     *
     * @param companyName nazwa firmy (może być null dla wszystkich)
     * @return nazwa zapisanego pliku
     * @throws IOException jeśli wystąpi błąd zapisu
     */
    public String saveCompanyCsvReport(String companyName) throws IOException {
        byte[] csvContent;
        String filename;
        
        if (companyName != null && !companyName.isEmpty()) {
            csvContent = generateCompanyCsvReport(companyName);
            filename = "report_" + companyName.replaceAll("[^a-zA-Z0-9]", "_") + ".csv";
        } else {
            csvContent = generateAllEmployeesCsvReport();
            filename = "report_all_employees.csv";
        }
        
        return fileStorageService.saveReportFile(filename, csvContent);
    }

    /**
     * Zapisuje raport PDF do katalogu raportów i zwraca nazwę pliku.
     *
     * @param companyName nazwa firmy
     * @return nazwa zapisanego pliku
     * @throws IOException jeśli wystąpi błąd zapisu
     */
    public String saveCompanyPdfReport(String companyName) throws IOException {
        byte[] pdfContent = generateCompanyStatisticsPdfReport(companyName);
        String filename = "statistics_" + companyName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        return fileStorageService.saveReportFile(filename, pdfContent);
    }
}
