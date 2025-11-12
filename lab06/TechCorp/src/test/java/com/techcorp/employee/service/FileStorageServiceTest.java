package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla FileStorageService.
 * Testuje operacje na plikach: zapisywanie, ładowanie, walidację.
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() throws IOException {
        // Utworzenie tymczasowych katalogów dla testów
        Path uploadsDir = tempDir.resolve("uploads");
        Path reportsDir = tempDir.resolve("reports");

        Files.createDirectories(uploadsDir);
        Files.createDirectories(reportsDir);

        // Inicjalizacja serwisu z tymczasowymi katalogami
        fileStorageService = new FileStorageService(
            uploadsDir.toString(),
            reportsDir.toString()
        );
    }

    @Nested
    class PhotoValidationTests {

        /**
         * Test walidacji poprawnego zdjęcia JPG.
         */
        @Test
        void testValidatePhotoFile_ValidJpg_NoException() {
            // Given
            MockMultipartFile jpgFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "fake jpg content".getBytes()
            );

            // When & Then
            assertDoesNotThrow(() -> fileStorageService.validatePhotoFile(jpgFile));
        }

        /**
         * Test walidacji poprawnego zdjęcia PNG.
         */
        @Test
        void testValidatePhotoFile_ValidPng_NoException() {
            // Given
            MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "fake png content".getBytes()
            );

            // When & Then
            assertDoesNotThrow(() -> fileStorageService.validatePhotoFile(pngFile));
        }

        /**
         * Test walidacji zdjęcia w nieprawidłowym formacie (GIF).
         */
        @Test
        void testValidatePhotoFile_InvalidFormat_ThrowsException() {
            // Given
            MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "photo.gif",
                "image/gif",
                "fake gif content".getBytes()
            );

            // When & Then
            assertThatThrownBy(() -> fileStorageService.validatePhotoFile(gifFile))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("format");
        }

        /**
         * Test walidacji zbyt dużego zdjęcia (>2MB).
         */
        @Test
        void testValidatePhotoFile_TooLarge_ThrowsException() {
            // Given - zdjęcie 3MB
            byte[] largeContent = new byte[3 * 1024 * 1024];
            MockMultipartFile largePhoto = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
            );

            // When & Then
            assertThatThrownBy(() -> fileStorageService.validatePhotoFile(largePhoto))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("2 MB");
        }

        /**
         * Test walidacji pustego zdjęcia.
         */
        @Test
        void testValidatePhotoFile_EmptyFile_ThrowsException() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
            );

            // When & Then
            assertThatThrownBy(() -> fileStorageService.validatePhotoFile(emptyFile))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("pusty");
        }
    }

    @Nested
    class FileSaveTests {

        /**
         * Test zapisywania przesłanego pliku.
         */
        @Test
        void testSaveUploadedFile_ValidFile_SavesSuccessfully() throws IOException {
            // Given
            String content = "test,data,content";
            MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                content.getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveUploadedFile(csvFile);

            // Then
            assertThat(savedFileName).isNotNull();
            assertThat(savedFileName).endsWith(".csv");
            
            // Sprawdź czy plik istnieje
            Path savedPath = fileStorageService.getUploadLocation().resolve(savedFileName);
            assertThat(Files.exists(savedPath)).isTrue();
            
            // Sprawdź zawartość pliku
            String savedContent = Files.readString(savedPath);
            assertThat(savedContent).isEqualTo(content);
        }

        /**
         * Test zapisywania pliku raportu.
         */
        @Test
        void testSaveReportFile_ValidData_SavesSuccessfully() throws IOException {
            // Given
            String filename = "report_test.csv";
            byte[] content = "report,data".getBytes();

            // When
            String savedFileName = fileStorageService.saveReportFile(filename, content);

            // Then
            assertThat(savedFileName).isNotNull();
            assertThat(savedFileName).contains("report_test");
            assertThat(savedFileName).endsWith(".csv");
            
            // Sprawdź czy plik istnieje w katalogu reports
            Path savedPath = tempDir.resolve("reports").resolve(savedFileName);
            assertThat(Files.exists(savedPath)).isTrue();
            assertThat(Files.readAllBytes(savedPath)).isEqualTo(content);
        }
    }

    @Nested
    class FileLoadTests {

        /**
         * Test ładowania istniejącego pliku.
         */
        @Test
        void testLoadFileFromPath_ExistingFile_ReturnsResource() throws IOException {
            // Given - najpierw zapisz plik
            String content = "test content";
            Path testFile = tempDir.resolve("uploads").resolve("test.csv");
            Files.writeString(testFile, content);

            // When
            var resource = fileStorageService.loadFileFromPath(testFile);

            // Then
            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
        }

        /**
         * Test ładowania nieistniejącego pliku.
         */
        @Test
        void testLoadFileFromPath_NonExistentFile_ThrowsException() {
            // Given
            Path nonExistentFile = tempDir.resolve("nonexistent.csv");

            // When & Then
            assertThatThrownBy(() -> fileStorageService.loadFileFromPath(nonExistentFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Nie można odczytać pliku");
        }
    }

    @Nested
    class EmployeePhotoTests {

        /**
         * Test zapisywania zdjęcia pracownika.
         */
        @Test
        void testSaveEmployeePhoto_ValidPhoto_SavesWithCorrectName() throws IOException {
            // Given
            String email = "jan.kowalski@example.com";
            MockMultipartFile photo = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake image data".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeePhoto(email, photo);

            // Then
            assertThat(savedFileName).isNotNull();
            assertThat(savedFileName).startsWith("jan.kowalski@example.com");
            assertThat(savedFileName).endsWith(".jpg");
        }

        /**
         * Test zapisywania zdjęcia PNG.
         */
        @Test
        void testSaveEmployeePhoto_PngPhoto_SavesWithPngExtension() throws IOException {
            // Given
            String email = "anna.nowak@example.com";
            MockMultipartFile photo = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "fake png data".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeePhoto(email, photo);

            // Then
            assertThat(savedFileName).endsWith(".png");
        }

        /**
         * Test że zdjęcie jest zapisywane w katalogu photos.
         */
        @Test
        void testSaveEmployeePhoto_ValidPhoto_SavesInPhotosDirectory() throws IOException {
            // Given
            String email = "test@example.com";
            MockMultipartFile photo = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "test photo".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeePhoto(email, photo);

            // Then
            Path photoPath = tempDir.resolve("uploads").resolve("photos").resolve(savedFileName);
            assertThat(Files.exists(photoPath)).isTrue();
        }
    }

    @Nested
    class EmployeeDocumentTests {

        /**
         * Test zapisywania dokumentu pracownika.
         */
        @Test
        void testSaveEmployeeDocument_ValidDocument_SavesSuccessfully() throws IOException {
            // Given
            String email = "jan@example.com";
            MockMultipartFile document = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeeDocument(email, document);

            // Then
            assertThat(savedFileName).isNotNull();
            assertThat(savedFileName).endsWith(".pdf");
        }

        /**
         * Test że dokument jest zapisywany w katalogu dokumentów pracownika.
         */
        @Test
        void testSaveEmployeeDocument_ValidDocument_SavesInEmployeeDirectory() throws IOException {
            // Given
            String email = "anna@example.com";
            MockMultipartFile document = new MockMultipartFile(
                "file",
                "certificate.pdf",
                "application/pdf",
                "certificate content".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeeDocument(email, document);

            // Then
            Path documentPath = tempDir.resolve("uploads")
                .resolve("documents")
                .resolve(email)
                .resolve(savedFileName);
            assertThat(Files.exists(documentPath)).isTrue();
        }

        /**
         * Test zachowania oryginalnego rozszerzenia pliku.
         */
        @Test
        void testSaveEmployeeDocument_DifferentExtensions_PreservesExtension() throws IOException {
            // Given - dokument PDF (dozwolone rozszerzenie)
            String email = "test@example.com";
            MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
            );

            // When
            String savedFileName = fileStorageService.saveEmployeeDocument(email, pdfFile);

            // Then
            assertThat(savedFileName).endsWith(".pdf");
        }
    }
}

