package com.techcorp.employee.controller;

import com.techcorp.employee.exception.GlobalExceptionHandler;
import com.techcorp.employee.exception.InvalidFileException;
import com.techcorp.employee.model.*;
import com.techcorp.employee.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy kontrolera FileUploadController z MockMultipartFile.
 */
@WebMvcTest
@ContextConfiguration(classes = {FileUploadController.class, GlobalExceptionHandler.class})
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ImportService importService;

    @MockBean
    private ReportGeneratorService reportGeneratorService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private EmployeeService employeeService;

    @Nested
    class CsvImportTests {

        /**
         * Test uploadu i importu pliku CSV.
         * Weryfikuje status 200 OK i zwrócone ImportSummary.
         */
        @Test
        void testImportCsv_ValidFile_ReturnsSuccess() throws Exception {
            // Given
            String csvContent = "firstName,lastName,email,company,position,salary\n" +
                               "Jan,Kowalski,jan@example.com,TechCorp,DEVELOPER,8000\n" +
                               "Anna,Nowak,anna@example.com,TechCorp,MANAGER,12000";
            
            MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
            );

            String savedFileName = "employees_uuid123.csv";
            ImportSummary expectedSummary = new ImportSummary();
            expectedSummary.importedCount();
            expectedSummary.importedCount();

            // When
            when(fileStorageService.getUploadLocation()).thenReturn(java.nio.file.Paths.get("uploads"));
            when(fileStorageService.saveUploadedFile(any())).thenReturn(savedFileName);
            when(importService.importFromCsv(anyString())).thenReturn(expectedSummary);

            // Then
            mockMvc.perform(multipart("/api/files/import/csv")
                    .file(csvFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Import zakończony sukcesem"))
                    .andExpect(jsonPath("$.importedCount").value(2))
                    .andExpect(jsonPath("$.errorCount").value(0));

            verify(fileStorageService).saveUploadedFile(any());
            verify(importService).importFromCsv(anyString());
        }

        /**
         * Test uploadu pliku CSV z błędami importu.
         * Weryfikuje że błędy są zwracane w odpowiedzi.
         */
        @Test
        void testImportCsv_WithImportErrors_ReturnsErrorsInResponse() throws Exception {
            // Given
            String csvContent = "firstName,lastName,email,company,position,salary\n" +
                               "Jan,Kowalski,jan@example.com,TechCorp,INVALID,8000";
            
            MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
            );

            ImportSummary summaryWithErrors = new ImportSummary();
            summaryWithErrors.addError(2, "Invalid position: INVALID");

            // When
            when(fileStorageService.getUploadLocation()).thenReturn(java.nio.file.Paths.get("uploads"));
            when(fileStorageService.saveUploadedFile(any())).thenReturn("employees.csv");
            when(importService.importFromCsv(anyString())).thenReturn(summaryWithErrors);

            // Then
            mockMvc.perform(multipart("/api/files/import/csv")
                    .file(csvFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Import zakończony z błędami"))
                    .andExpect(jsonPath("$.importedCount").value(0))
                    .andExpect(jsonPath("$.errorCount").value(1))
                    .andExpect(jsonPath("$.errors[0]").value(containsString("Invalid position")));
        }

        /**
         * Test uploadu pliku z nieprawidłowym rozszerzeniem.
         * Weryfikuje status 400 Bad Request.
         */
        @Test
        void testImportCsv_InvalidExtension_ReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "data.txt",
                "text/plain",
                "Some text content".getBytes(StandardCharsets.UTF_8)
            );

            // When - kontroler sam waliduje rozszerzenie
            // Nie mockujemy getUploadLocation bo kontroler nawet do tego nie dojdzie

            // Then
            mockMvc.perform(multipart("/api/files/import/csv")
                    .file(txtFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Błąd walidacji pliku"))
                    .andExpect(jsonPath("$.message").value(containsString("rozszerzenie .csv")));
        }

        /**
         * Test uploadu zbyt dużego pliku.
         * Weryfikuje status 400 Bad Request (kontroler waliduje rozmiar).
         */
        @Test
        void testImportCsv_FileTooLarge_ReturnsBadRequest() throws Exception {
            // Given - symulacja dużego pliku (rozmiar jest sprawdzany w kontrolerze)
            byte[] largeContent = new byte[11 * 1024 * 1024];
            Arrays.fill(largeContent, (byte) 'A');
            
            MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                largeContent
            );

            // When - kontroler sam waliduje rozmiar przed zapisem
            when(fileStorageService.getUploadLocation()).thenReturn(java.nio.file.Paths.get("uploads"));
            when(fileStorageService.saveUploadedFile(any())).thenReturn("large.csv");
            when(importService.importFromCsv(anyString())).thenThrow(
                new IllegalArgumentException("Plik jest za duży")
            );

            // Then
            mockMvc.perform(multipart("/api/files/import/csv")
                    .file(largeFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("za duży")));
        }
    }

    @Nested
    class XmlImportTests {

        /**
         * Test uploadu i importu pliku XML.
         */
        @Test
        void testImportXml_ValidFile_ReturnsSuccess() throws Exception {
            // Given
            String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <employees>
                    <employee>
                        <firstName>Jan</firstName>
                        <lastName>Kowalski</lastName>
                        <email>jan@example.com</email>
                        <company>TechCorp</company>
                        <position>DEVELOPER</position>
                        <salary>8000</salary>
                    </employee>
                </employees>
                """;
            
            MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "employees.xml",
                "application/xml",
                xmlContent.getBytes(StandardCharsets.UTF_8)
            );

            ImportSummary expectedSummary = new ImportSummary();
            expectedSummary.importedCount();

            // When
            when(fileStorageService.getUploadLocation()).thenReturn(java.nio.file.Paths.get("uploads"));
            when(fileStorageService.saveUploadedFile(any())).thenReturn("employees.xml");
            when(importService.importFromXml(anyString())).thenReturn(expectedSummary);

            // Then
            mockMvc.perform(multipart("/api/files/import/xml")
                    .file(xmlFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.importedCount").value(1));

            verify(importService).importFromXml(anyString());
        }
    }

    @Nested
    class CsvExportTests {

        /**
         * Test downloadu raportu CSV ze wszystkimi pracownikami.
         * Weryfikuje status 200 OK, Content-Type i zawartość.
         */
        @Test
        void testExportCsv_AllEmployees_ReturnsCSVFile() throws Exception {
            // Given
            String csvContent = "First Name,Last Name,Email,Company,Position,Salary\n" +
                               "Jan,Kowalski,jan@example.com,TechCorp,DEVELOPER,8000\n" +
                               "Anna,Nowak,anna@example.com,TechCorp,MANAGER,12000";
            
            byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

            // When
            when(reportGeneratorService.generateAllEmployeesCsvReport()).thenReturn(csvBytes);

            // Then
            mockMvc.perform(get("/api/files/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", 
                              containsString("attachment; filename=\"employees_all.csv\"")))
                    .andExpect(header().string("Content-Type", "text/csv"))
                    .andExpect(content().string(containsString("Jan,Kowalski")))
                    .andExpect(content().string(containsString("Anna,Nowak")))
                    .andExpect(content().bytes(csvBytes));

            verify(reportGeneratorService).generateAllEmployeesCsvReport();
        }

        /**
         * Test downloadu raportu CSV dla konkretnej firmy.
         * Weryfikuje parametr query company.
         */
        @Test
        void testExportCsv_ForCompany_ReturnsFilteredCSV() throws Exception {
            // Given
            String csvContent = "First Name,Last Name,Email,Company,Position,Salary\n" +
                               "Jan,Kowalski,jan@example.com,TechCorp,DEVELOPER,8000";
            
            byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

            // When
            when(reportGeneratorService.generateCompanyCsvReport("TechCorp")).thenReturn(csvBytes);

            // Then
            mockMvc.perform(get("/api/files/export/csv")
                    .param("company", "TechCorp"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", 
                              containsString("employees_TechCorp.csv")))
                    .andExpect(content().string(containsString("TechCorp")));

            verify(reportGeneratorService).generateCompanyCsvReport("TechCorp");
            verify(reportGeneratorService, never()).generateAllEmployeesCsvReport();
        }
    }

    @Nested
    class PdfReportTests {

        /**
         * Test generowania raportu PDF ze statystykami firmy.
         */
        @Test
        void testGetCompanyStatisticsReport_ValidCompany_ReturnsPDF() throws Exception {
            // Given
            byte[] pdfContent = "PDF content".getBytes();

            // When
            when(reportGeneratorService.generateCompanyStatisticsPdfReport("TechCorp"))
                .thenReturn(pdfContent);

            // Then
            mockMvc.perform(get("/api/files/reports/statistics/TechCorp"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(header().string("Content-Disposition", 
                              containsString("statistics_TechCorp.pdf")))
                    .andExpect(content().bytes(pdfContent));

            verify(reportGeneratorService).generateCompanyStatisticsPdfReport("TechCorp");
        }

        /**
         * Test raportu PDF dla nieistniejącej firmy.
         */
        @Test
        void testGetCompanyStatisticsReport_NonExistentCompany_ReturnsNotFound() throws Exception {
            // When
            when(reportGeneratorService.generateCompanyStatisticsPdfReport("NonExistent"))
                .thenThrow(new IllegalArgumentException("Firma nie istnieje"));

            // Then
            mockMvc.perform(get("/api/files/reports/statistics/NonExistent"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class EmployeeDocumentTests {

        /**
         * Test uploadu dokumentu pracownika.
         * Weryfikuje status 201 Created i zwrócone metadane.
         */
        @Test
        void testUploadEmployeeDocument_ValidFile_ReturnsCreated() throws Exception {
            // Given
            MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "PDF content".getBytes()
            );

            String email = "jan@example.com";
            DocumentType docType = DocumentType.CONTRACT;

            EmployeeDocument expectedDoc = new EmployeeDocument(
                email, 
                "contract_uuid.pdf", 
                "contract.pdf", 
                docType,
                "/uploads/documents/jan@example.com/contract_uuid.pdf"
            );

            // When
            when(documentService.saveDocument(eq(email), any(), eq(docType)))
                .thenReturn(expectedDoc);

            // Then
            mockMvc.perform(multipart("/api/files/documents/{email}", email)
                    .file(pdfFile)
                    .param("type", "CONTRACT"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.employeeEmail").value(email))
                    .andExpect(jsonPath("$.originalFileName").value("contract.pdf"))
                    .andExpect(jsonPath("$.fileType").value("CONTRACT"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.uploadDate").exists());

            verify(documentService).saveDocument(eq(email), any(), eq(docType));
        }

        /**
         * Test uploadu dokumentu dla nieistniejącego pracownika.
         */
        @Test
        void testUploadEmployeeDocument_NonExistentEmployee_ReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "PDF content".getBytes()
            );

            // When
            when(documentService.saveDocument(anyString(), any(), any()))
                .thenThrow(new IllegalArgumentException("Pracownik nie istnieje"));

            // Then
            mockMvc.perform(multipart("/api/files/documents/unknown@example.com")
                    .file(pdfFile)
                    .param("type", "CONTRACT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Błąd walidacji"))
                    .andExpect(jsonPath("$.message").value(containsString("nie istnieje")));
        }

        /**
         * Test pobierania listy dokumentów pracownika.
         */
        @Test
        void testGetEmployeeDocuments_ReturnsListOfDocuments() throws Exception {
            // Given
            String email = "jan@example.com";
            
            EmployeeDocument doc1 = new EmployeeDocument(
                email, "contract.pdf", "Umowa.pdf", 
                DocumentType.CONTRACT, "/path/contract.pdf"
            );
            EmployeeDocument doc2 = new EmployeeDocument(
                email, "cert.pdf", "Certyfikat.pdf", 
                DocumentType.CERTIFICATE, "/path/cert.pdf"
            );

            List<EmployeeDocument> documents = Arrays.asList(doc1, doc2);

            // When
            when(documentService.getEmployeeDocuments(email)).thenReturn(documents);

            // Then
            mockMvc.perform(get("/api/files/documents/{email}", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].fileType").value("CONTRACT"))
                    .andExpect(jsonPath("$[1].fileType").value("CERTIFICATE"));

            verify(documentService).getEmployeeDocuments(email);
        }

        /**
         * Test downloadu konkretnego dokumentu pracownika.
         */
        @Test
        void testDownloadEmployeeDocument_ValidDocument_ReturnsFile() throws Exception {
            // Given
            String email = "jan@example.com";
            String docId = "doc-123";
            
            EmployeeDocument document = new EmployeeDocument(
                email, "contract_uuid.pdf", "Umowa.pdf", 
                DocumentType.CONTRACT, "/path/contract.pdf"
            );

            Resource resource = new ByteArrayResource("PDF content".getBytes());

            // When
            when(documentService.getEmployeeDocument(email, docId)).thenReturn(document);
            when(documentService.loadDocumentFile(docId)).thenReturn(resource);

            // Then
            mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", 
                              containsString("attachment; filename=\"Umowa.pdf\"")))
                    .andExpect(content().string("PDF content"));

            verify(documentService).getEmployeeDocument(email, docId);
            verify(documentService).loadDocumentFile(docId);
        }

        /**
         * Test usuwania dokumentu pracownika.
         */
        @Test
        void testDeleteEmployeeDocument_ValidDocument_ReturnsNoContent() throws Exception {
            // Given
            String email = "jan@example.com";
            String docId = "doc-123";

            // When
            when(documentService.deleteDocument(email, docId)).thenReturn(true);

            // Then
            mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", email, docId))
                    .andExpect(status().isNoContent());

            verify(documentService).deleteDocument(email, docId);
        }
    }

    @Nested
    class PhotoUploadTests {

        /**
         * Test uploadu zdjęcia profilowego.
         * Weryfikuje walidację formatu i rozmiaru.
         */
        @Test
        void testUploadEmployeePhoto_ValidPhoto_ReturnsSuccess() throws Exception {
            // Given
            MockMultipartFile photoFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake image content".getBytes()
            );

            String email = "jan@example.com";
            String photoFileName = "jan@example.com.jpg";

            Employee employee = new Employee(
                "Jan", "Kowalski", email, "TechCorp", Position.DEVELOPER
            );

            // When
            when(employeeService.getAllEmployees()).thenReturn(List.of(employee));
            when(fileStorageService.saveEmployeePhoto(eq(email), any())).thenReturn(photoFileName);

            // Then
            mockMvc.perform(multipart("/api/files/photos/{email}", email)
                    .file(photoFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("zapisane")))
                    .andExpect(jsonPath("$.photoFileName").value(photoFileName))
                    .andExpect(jsonPath("$.employeeEmail").value(email));

            verify(fileStorageService).saveEmployeePhoto(eq(email), any());
        }

        /**
         * Test uploadu zdjęcia dla nieistniejącego pracownika.
         */
        @Test
        void testUploadEmployeePhoto_NonExistentEmployee_ReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile photoFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake image content".getBytes()
            );

            // When
            when(employeeService.getAllEmployees()).thenReturn(List.of());

            // Then
            mockMvc.perform(multipart("/api/files/photos/unknown@example.com")
                    .file(photoFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Błąd walidacji"))
                    .andExpect(jsonPath("$.message").value(containsString("nie istnieje")));
        }

        /**
         * Test uploadu zdjęcia w niewłaściwym formacie.
         */
        @Test
        void testUploadEmployeePhoto_InvalidFormat_ReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "profile.gif",
                "image/gif",
                "fake gif content".getBytes()
            );

            String email = "jan@example.com";
            Employee employee = new Employee(
                "Jan", "Kowalski", email, "TechCorp", Position.DEVELOPER
            );

            // When
            when(employeeService.getAllEmployees()).thenReturn(List.of(employee));
            when(fileStorageService.saveEmployeePhoto(eq(email), any()))
                .thenThrow(new InvalidFileException("Niedozwolony format zdjęcia: .gif"));

            // Then
            mockMvc.perform(multipart("/api/files/photos/{email}", email)
                    .file(gifFile))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(containsString("format")));
        }

        /**
         * Test pobierania zdjęcia profilowego.
         */
        @Test
        void testGetEmployeePhoto_ExistingPhoto_ReturnsImage() throws Exception {
            // Given
            String email = "jan@example.com";
            String photoFileName = "jan@example.com.jpg";
            
            Employee employee = new Employee(
                "Jan", "Kowalski", email, "TechCorp", Position.DEVELOPER
            );
            employee.setPhotoFileName(photoFileName);

            Resource photoResource = new ByteArrayResource("image data".getBytes());

            // When
            when(employeeService.getAllEmployees()).thenReturn(List.of(employee));
            when(fileStorageService.loadEmployeePhoto(photoFileName)).thenReturn(photoResource);

            // Then
            mockMvc.perform(get("/api/files/photos/{email}", email))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"))
                    .andExpect(header().string("Content-Disposition", 
                              containsString("inline; filename=\"" + photoFileName + "\"")))
                    .andExpect(content().bytes("image data".getBytes()));

            verify(fileStorageService).loadEmployeePhoto(photoFileName);
        }

        /**
         * Test pobierania zdjęcia gdy pracownik nie ma zdjęcia.
         */
        @Test
        void testGetEmployeePhoto_NoPhoto_ReturnsNotFound() throws Exception {
            // Given
            String email = "jan@example.com";
            
            Employee employee = new Employee(
                "Jan", "Kowalski", email, "TechCorp", Position.DEVELOPER
            );
            // Brak photoFileName

            // When
            when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

            // Then
            mockMvc.perform(get("/api/files/photos/{email}", email))
                    .andExpect(status().isNotFound());

            verify(fileStorageService, never()).loadEmployeePhoto(anyString());
        }
    }
}
