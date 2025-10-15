# TechCorp - system zarządzania

System do zarządzania pracownikami z obsługą importu z plików CSV, integracją z API oraz funkcjami analitycznymi.

## Funkcje
- Dodawanie, wyszukiwanie, sortowanie i grupowanie pracowników
- Import danych z pliku CSV (z walidacją i raportowaniem błędów)
- Integracja z zewnętrznym API (https://jsonplaceholder.typicode.com/users)
- Analizy i statystyki dla firm: liczba pracowników, średnie wynagrodzenie, najlepiej zarabiający
- Walidacja spójności wynagrodzeń

## Wymagania techniczne
- Java 11 lub nowsza
- Biblioteka Gson (`com.google.code.gson:gson:2.10.1`)

## Instalacja i uruchomienie

### 1. Klonowanie repozytorium

```bash
git clone <adres_repozytorium>
cd lab02/TechCorp
```

### 2. Uruchomienie aplikacji z poziomu IDE

1. Otwórz projekt w swoim IDE (np. IntelliJ IDEA).
2. Odszukaj klasę `Main` (`src/main/techcorp`).
3. Kliknij prawym przyciskiem myszy na plik `Main.java` i wybierz opcję **Run 'Main'** lub odpowiednik tej opcji w Twoim IDE.
4. Gotowe!

**Uwaga:**  
Przy pierwszym uruchomieniu, jeśli plik `employees.csv` nie istnieje w katalogu projektu, zostanie automatycznie utworzony z przykładowymi danymi. Możesz później edytować ten plik lub podmienić go swoimi danymi.