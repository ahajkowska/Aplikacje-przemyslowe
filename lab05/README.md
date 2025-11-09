# Employee Management System - Spring Boot

## 📋 Spis treści

1. [Opis projektu](#opis-projektu)
2. [Migracja do Spring Boot](#migracja-do-spring-boot)
3. [Definiowanie beanów](#definiowanie-beanów)
4. [Wymagania](#wymagania)
5. [Uruchomienie aplikacji](#uruchomienie-aplikacji)
6. [Testowanie aplikacji](#testowanie)

---

## Opis projektu

Aplikacja Spring Boot do zarządzania danymi pracowników z trzech źródeł:
- **CSV** (plik w resources)
- **XML** (konfiguracja Spring)
- **REST API** (zewnętrzne źródło)

### Przykładowe funkcjonalności:
- Import pracowników (CSV, XML, REST API)
- Wyświetlanie listy pracowników
- Statystyki (średnie wynagrodzenie, najwyższe wynagrodzenie)
- Statystyki firm
- Analiza stanowisk
- Walidacja wynagrodzeń

---

## Migracja do Spring Boot

### Przed migracją (tradycyjna Java):

- Ręczne tworzenie obiektów
- Trudne testowanie

### Po migracji (SpringBoot)

- Automatyczne zarządzanie beanami
- Dependency Injection
- Łatwe testowanie

### Kluczowe kroki migracji:
Dodanie Spring Boot do pom.xml
Dodanie adnotacji (@Service, @Component)
Utworzenie głównej klasy z @SpringBootApplication
Externalizacja konfiguracji (application.properties)

## Definiowanie beanów
Spring Boot oferuje 3 sposoby definiowania beanów:

### Adnotacje (najczęstsze)
```
@Service  // Bean dla logiki biznesowej
public class EmployeeService {
    
    public List<Employee> getAllEmployees() {
        // Logika
    }
}
```

Dostępne adnotacje:

@Component - ogólny komponent
@Service - logika biznesowa
@Repository - dostęp do danych
@Controller - kontroler web

Zalety: Najprostsze, automatyczne skanowanie
Wady: Mniejsza kontrola

### Klasy konfiguracyjne (elastyczne)

```
@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public DataSource dataSource() {
        // Konfiguracja DataSource
        return new DriverManagerDataSource();
    }
}
```
Zalety: Pełna kontrola, idealne dla bibliotek zewnętrznych
Wady: Więcej kodu

### XML
Import xml:
```
@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication {
    // ...
}
```
Zalety: Oddzielenie konfiguracji od kodu
Wady: Verbose, brak type-safety, przestarzałe

## Wymagania
Java: 17+
Maven: 3.6+

## Uruchomienie aplikacji

# 1. Zbuduj projekt
```mvn clean install```

# 2. Uruchom
```mvn spring-boot:run```


## Testowanie aplikacji

Test podstawowy:
```mvn spring-boot:run```

Testy jednostkowe:
```mvn test```
