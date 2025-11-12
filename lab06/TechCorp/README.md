# TechCorp Employee Management System

System zarządzania pracownikami z obsługą plików CSV/XML, generowaniem raportów PDF oraz zarządzaniem dokumentami i zdjęciami pracowników.

## Spis treści

- [Architektura przechowywania plików](#architektura-przechowywania-plików)
- [Konfiguracja katalogów](#konfiguracja-katalogów)
- [Endpointy API](#endpointy-api)
- [Przykłady użycia (curl)](#przykłady-użycia-curl)
- [Uruchomienie projektu](#uruchomienie-projektu)
- [Testowanie](#testowanie)

---

## Architektura przechowywania plików

### Struktura katalogów

```
uploads/                          # Katalog główny dla przesłanych plików
├── *.csv, *.xml                  # Pliki importu (CSV/XML)
├── photos/                       # Zdjęcia profilowe pracowników
│   └── {email}.{jpg|png}         # Nazwane według email pracownika
└── documents/                    # Dokumenty pracowników
    └── {email}/                  # Folder per pracownik
        └── {documentId}.{ext}    # Dokumenty z UUID

reports/                          # Katalog dla generowanych raportów
├── *.csv                         # Raporty CSV
└── *.pdf                         # Raporty PDF ze statystykami
```

### Zabezpieczenia

1. **Walidacja rozszerzeń plików** - tylko dozwolone typy (.csv, .xml, .pdf, .jpg, .png, etc.)
2. **Walidacja rozmiaru**:
   - Pliki ogólne: max **10 MB**
   - Zdjęcia profilowe: max **2 MB**
3. **Walidacja MIME type** - podwójna weryfikacja (rozszerzenie + Content-Type)
4. **Ochrona przed Path Traversal** - normalizacja ścieżek
5. **Unique filenames** - UUID zapobiega konfliktom nazw

### Typy przechowywanych plików

| Typ pliku | Format | Lokalizacja | Limit rozmiaru |
|-----------|--------|-------------|----------------|
| Import CSV/XML | `.csv`, `.xml` | `uploads/` | 10 MB |
| Raporty CSV | `.csv` | `reports/` | Brak |
| Raporty PDF | `.pdf` | `reports/` | Brak |
| Dokumenty pracowników | `.pdf`, `.docx`, `.xlsx`, etc. | `uploads/documents/{email}/` | 10 MB |
| Zdjęcia profilowe | `.jpg`, `.png` | `uploads/photos/` | 2 MB |

---

## Konfiguracja katalogów

### 1. Konfiguracja w `application.properties`

```properties
# Upload plików
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Katalogi
app.upload.directory=uploads/
app.reports.directory=reports/
```

### 2. Automatyczne tworzenie katalogów

Katalogi są automatycznie tworzone przy starcie aplikacji przez `FileStorageService`:

```java
@PostConstruct
public void init() {
    try {
        Files.createDirectories(this.uploadLocation);
        Files.createDirectories(this.reportLocation);
        Files.createDirectories(this.uploadLocation.resolve("photos"));
        Files.createDirectories(this.uploadLocation.resolve("documents"));
    } catch (IOException e) {
        throw new FileStorageException("Nie można utworzyć katalogów...");
    }
}
```

### 3. Ręczne utworzenie (opcjonalnie)

```bash
mkdir uploads
mkdir uploads/photos
mkdir uploads/documents
mkdir reports
```

---

## Endpointy API

### Import plików CSV/XML

#### 1. Import pracowników z CSV
```
POST /api/files/import/csv
Content-Type: multipart/form-data
```

**Parametry:**
- `file` - plik CSV (required)

**Odpowiedź:** `ImportSummary` z liczbą zaimportowanych rekordów i błędami

---

#### 2. Import pracowników z XML
```
POST /api/files/import/xml
Content-Type: multipart/form-data
```

**Parametry:**
- `file` - plik XML (required)

**Odpowiedź:** `ImportSummary`

---

### Export/Raporty

#### 3. Export pracowników do CSV
```
GET /api/files/export/csv?company={companyName}
```

**Parametry:**
- `company` - nazwa firmy (optional, brak = wszyscy pracownicy)

**Odpowiedź:** Plik CSV do pobrania

---

#### 4. Raport PDF ze statystykami firmy
```
GET /api/files/reports/statistics/{companyName}
```

**Parametry:**
- `companyName` - nazwa firmy (path variable)

**Odpowiedź:** Plik PDF ze statystykami (liczba pracowników, średnia płaca, lista pracowników)

---

### Dokumenty pracowników

#### 5. Przesłanie dokumentu pracownika
```
POST /api/files/documents/{email}
Content-Type: multipart/form-data
```

**Parametry:**
- `email` - email pracownika (path variable)
- `file` - plik dokumentu (required)
- `type` - typ dokumentu: `CONTRACT`, `CERTIFICATE`, `ID_CARD`, `OTHER` (required)

**Odpowiedź:** 201 Created + `EmployeeDocument` JSON

---

#### 6. Lista dokumentów pracownika
```
GET /api/files/documents/{email}
```

**Odpowiedź:** Array of `EmployeeDocument`

---

#### 7. Pobranie konkretnego dokumentu
```
GET /api/files/documents/{email}/{documentId}
```

**Odpowiedź:** Plik do pobrania

---

#### 8. Usunięcie dokumentu
```
DELETE /api/files/documents/{email}/{documentId}
```

**Odpowiedź:** 204 No Content

---

### Zdjęcia profilowe

#### 9. Przesłanie zdjęcia profilowego
```
POST /api/files/photos/{email}
Content-Type: multipart/form-data
```

**Parametry:**
- `email` - email pracownika (path variable)
- `file` - plik zdjęcia JPG/PNG, max 2MB (required)

**Odpowiedź:** JSON z nazwą zapisanego pliku

---

#### 10. Pobranie zdjęcia profilowego
```
GET /api/files/photos/{email}
```

**Odpowiedź:** Plik obrazu (Content-Type: image/jpeg lub image/png)

---

## Przykłady użycia (curl)

### 1. Import pracowników z CSV

```bash
curl -X POST http://localhost:8080/api/files/import/csv \
  -F "file=@employees.csv" \
  -H "Content-Type: multipart/form-data"
```

**Przykładowa odpowiedź:**
```json
{
  "success": true,
  "message": "Import zakończony sukcesem",
  "importedCount": 10,
  "errorCount": 0,
  "errors": []
}
```

---

### 2. Import pracowników z XML

```bash
curl -X POST http://localhost:8080/api/files/import/xml \
  -F "file=@test-employees.xml" \
  -H "Content-Type: multipart/form-data"
```

---

### 3. Export wszystkich pracowników do CSV

```bash
curl -X GET http://localhost:8080/api/files/export/csv \
  -o employees_all.csv
```

---

### 4. Export pracowników firmy do CSV

```bash
curl -X GET "http://localhost:8080/api/files/export/csv?company=TechCorp" \
  -o employees_techcorp.csv
```

---

### 5. Raport PDF ze statystykami firmy

```bash
curl -X GET http://localhost:8080/api/files/reports/statistics/TechCorp \
  -o statistics_techcorp.pdf
```

---

### 6. Przesłanie dokumentu pracownika

```bash
curl -X POST http://localhost:8080/api/files/documents/jan.kowalski@techcorp.com \
  -F "file=@contract.pdf" \
  -F "type=CONTRACT"
```

**Przykładowa odpowiedź:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "employeeEmail": "jan.kowalski@techcorp.com",
  "originalFileName": "contract.pdf",
  "storedFileName": "550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileType": "CONTRACT",
  "uploadDate": "2025-11-09T22:30:00",
  "filePath": "/uploads/documents/jan.kowalski@techcorp.com/550e8400-e29b-41d4-a716-446655440000.pdf"
}
```

---

### 7. Lista dokumentów pracownika

```bash
curl -X GET http://localhost:8080/api/files/documents/jan.kowalski@techcorp.com
```

---

### 8. Pobranie dokumentu pracownika

```bash
curl -X GET http://localhost:8080/api/files/documents/jan.kowalski@techcorp.com/550e8400-e29b-41d4-a716-446655440000 \
  -o downloaded_contract.pdf
```

---

### 9. Usunięcie dokumentu

```bash
curl -X DELETE http://localhost:8080/api/files/documents/jan.kowalski@techcorp.com/550e8400-e29b-41d4-a716-446655440000
```

---

### 10. Przesłanie zdjęcia profilowego

```bash
curl -X POST http://localhost:8080/api/files/photos/jan.kowalski@techcorp.com \
  -F "file=@profile_photo.jpg" \
  -H "Content-Type: multipart/form-data"
```

**Przykładowa odpowiedź:**
```json
{
  "success": true,
  "message": "Zdjęcie profilowe zostało zapisane",
  "photoFileName": "jan.kowalski@techcorp.com_a1b2c3d4.jpg",
  "employeeEmail": "jan.kowalski@techcorp.com"
}
```

---

### 11. Pobranie zdjęcia profilowego

```bash
curl -X GET http://localhost:8080/api/files/photos/jan.kowalski@techcorp.com \
  -o profile_photo.jpg
```

---

## 🏃 Uruchomienie projektu

### 1. Kompilacja

```bash
mvn clean compile
```

### 2. Uruchomienie aplikacji

```bash
mvn spring-boot:run
```

Aplikacja będzie dostępna pod adresem: **http://localhost:8080**

### 3. Alternatywnie - JAR

```bash
mvn clean package
java -jar target/TechCorp-1.0-SNAPSHOT.jar
```

---

## Testowanie

### Uruchomienie wszystkich testów

```bash
mvn test
```

### Uruchomienie konkretnego testu

```bash
mvn test -Dtest=FileUploadControllerTest
mvn test -Dtest=FileStorageServiceTest
```

### Raport pokrycia kodu (JaCoCo)

```bash
mvn clean test jacoco:report
```

Raport będzie dostępny w: `target/site/jacoco/index.html`

### Statystyki testów

- **Łączna liczba testów:** 183
- **FileUploadControllerTest:** 19 testów (kontroler z MockMultipartFile)
- **FileStorageServiceTest:** 12 testów (serwis z @TempDir)
- **EmployeeServiceTest:** 58 testów
- **ImportServiceTest:** 43 testów
- **ApiServiceTest:** 28 testów
- **EmployeeControllerTest:** 17 testów
- **StatisticsControllerTest:** 6 testów

---

## Przykładowe pliki

### employees.csv

```csv
firstName,lastName,email,company,position,salary
Jan,Kowalski,jan.kowalski@techcorp.com,TechCorp,DEVELOPER,8000
Anna,Nowak,anna.nowak@techcorp.com,TechCorp,MANAGER,12000
Piotr,Wiśniewski,piotr.wisniewski@innovate.com,InnovateCorp,DEVELOPER,7500
```

### test-employees.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<employees>
    <employee>
        <firstName>Jan</firstName>
        <lastName>Kowalski</lastName>
        <email>jan.kowalski@techcorp.com</email>
        <company>TechCorp</company>
        <position>DEVELOPER</position>
        <salary>8000</salary>
    </employee>
    <employee>
        <firstName>Anna</firstName>
        <lastName>Nowak</lastName>
        <email>anna.nowak@techcorp.com</email>
        <company>TechCorp</company>
        <position>MANAGER</position>
        <salary>12000</salary>
    </employee>
</employees>
```

---

## 🛡️ Obsługa błędów

### Kody odpowiedzi HTTP

| Kod | Znaczenie | Przykład |
|-----|-----------|----------|
| 200 | OK | Pobrano plik, import zakończony |
| 201 | Created | Dokument został dodany |
| 204 | No Content | Dokument usunięty |
| 400 | Bad Request | Nieprawidłowe rozszerzenie pliku, walidacja |
| 404 | Not Found | Pracownik nie istnieje, plik nie znaleziony |
| 413 | Payload Too Large | Plik przekracza limit rozmiaru |
| 500 | Internal Server Error | Błąd zapisu na dysku |

### Przykłady błędów

#### Nieprawidłowe rozszerzenie
```json
{
  "error": "Błąd walidacji pliku",
  "message": "Plik musi mieć rozszerzenie .csv"
}
```

#### Plik zbyt duży
```json
{
  "error": "Plik za duży",
  "message": "Maksymalny rozmiar pliku to 10 MB"
}
```

#### Pracownik nie istnieje
```json
{
  "error": "Błąd walidacji",
  "message": "Pracownik o emailu unknown@example.com nie istnieje"
}
```

---

## Dokumentacja kodu

### Kluczowe komponenty

#### FileStorageService
Główny serwis do zarządzania plikami:
- `saveUploadedFile()` - zapis ogólnych plików
- `saveEmployeePhoto()` - zapis zdjęć z walidacją MIME
- `saveEmployeeDocument()` - zapis dokumentów pracowników
- `validatePhotoFile()` - walidacja formatu i rozmiaru zdjęć
- `loadFileFromPath()` - ładowanie pliku jako Resource

#### FileUploadController
REST kontroler z endpointami:
- Import CSV/XML
- Export CSV
- Raporty PDF
- CRUD dokumentów
- Upload/download zdjęć

#### ReportGeneratorService
Generowanie raportów:
- `generateAllEmployeesCsvReport()` - CSV wszystkich pracowników
- `generateCompanyCsvReport()` - CSV dla firmy
- `generateCompanyStatisticsPdfReport()` - PDF ze statystykami

#### DocumentService
Zarządzanie metadanymi dokumentów (in-memory):
- `saveDocument()` - zapisz metadane
- `getEmployeeDocuments()` - lista dokumentów pracownika
- `deleteDocument()` - usuń dokument i metadane

---

## Bezpieczeństwo

1. **Walidacja wejścia:**
   - Sprawdzanie rozszerzeń plików
   - Weryfikacja MIME type
   - Limit rozmiaru plików

2. **Ochrona ścieżek:**
   - Normalizacja ścieżek (`Path.normalize()`)
   - Sprawdzanie czy ścieżka nie wychodzi poza katalog

3. **Unikalność nazw:**
   - UUID dla dokumentów
   - Email + UUID dla zdjęć

4. **Obsługa wyjątków:**
   - Dedykowane wyjątki: `FileStorageException`, `InvalidFileException`, `FileNotFoundException`
   - Globalna obsługa przez `GlobalExceptionHandler`
