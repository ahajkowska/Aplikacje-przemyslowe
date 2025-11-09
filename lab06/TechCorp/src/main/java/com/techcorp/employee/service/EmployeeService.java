package com.techcorp.employee.service;

import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final Set<Employee> employees;

    public EmployeeService() {
        this.employees = new HashSet<>();
    }

    // Dodawanie nowego pracownika do systemu z walidacją unikalności adresu email przed dodaniem
    public boolean addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (isEmailTaken(employee.getEmail())) {
            throw new DuplicateEmailException(employee.getEmail());
        }
        return employees.add(employee);
    }

    private boolean isEmailTaken(String email) {
        return employees.stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(email));
    }

    // Wyświetlanie listy wszystkich pracowników w systemie
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    // Wyszukiwanie pracownika po adresie email
    public Optional<Employee> findEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        return employees.stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // Aktualizacja danych pracownika
    public Optional<Employee> updateEmployee(String email, Employee updatedEmployee) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (updatedEmployee == null) {
            throw new IllegalArgumentException("Updated employee cannot be null");
        }
        
        Optional<Employee> existingEmployee = findEmployeeByEmail(email);
        if (existingEmployee.isPresent()) {
            employees.remove(existingEmployee.get());
            employees.add(updatedEmployee);
            return Optional.of(updatedEmployee);
        }
        return Optional.empty();
    }

    // Usuwanie pracownika z systemu
    public boolean deleteEmployee(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        
        Optional<Employee> employee = findEmployeeByEmail(email);
        if (employee.isPresent()) {
            return employees.remove(employee.get());
        }
        return false;
    }

    // Aktualizacja statusu pracownika
    public Optional<Employee> updateEmployeeStatus(String email, EmploymentStatus newStatus) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Optional<Employee> employee = findEmployeeByEmail(email);
        if (employee.isPresent()) {
            employee.get().setStatus(newStatus);
            return employee;
        }
        return Optional.empty();
    }

    // Wyszukiwanie pracowników po statusie zatrudnienia
    public List<Employee> findEmployeesByStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return employees.stream()
                .filter(e -> e.getStatus() == status)
                .toList();
    }

    // Wyszukiwanie pracowników zatrudnionych w konkretnej firmie - zaimplementuj jako operacje filtrowania kolekcji z wykorzystaniem Stream API.
    public List<Employee> findEmployeesInCompany(String company) {
        if (company == null || company.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be null or blank");
        }
        return employees.stream()
                .filter(e -> e.getCompany().equals(company))
                .toList();
    }

    // Prezentacja pracowników w kolejności alfabetycznej według nazwiska - użyj Comparator do zdefiniowania porządku sortowania.
    public List<Employee> getEmployeesSortedByLastName() {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::getLastName))
                .toList();
    }

    // Grupowanie pracowników według zajmowanego stanowiska - operacja powinna zwrócić strukturę Map, gdzie kluczem jest stanowisko, a wartością lista pracowników na tym stanowisku.
    public Map<Position, List<Employee>> getEmployeesGroupedByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    // Zliczanie liczby pracowników na każdym stanowisku - wynik w formie Map mapującej stanowisko na liczbę pracowników.
    public Map<Position, Long> countEmployeesOnPositions() {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getPosition, Collectors.counting()));
    }

    // Obliczanie średniego wynagrodzenia w całej organizacji - operacja agregująca dane finansowe wszystkich pracowników.
    public double averageSalary() {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0);
    }

    // Obliczanie średniego wynagrodzenia w konkretnej firmie
    public double averageSalaryByCompany(String company) {
        if (company == null || company.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be null or blank");
        }
        return employees.stream()
                .filter(e -> e.getCompany().equals(company))
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0);
    }

    // Pobieranie najwyższego wynagrodzenia w konkretnej firmie
    public Double getHighestSalaryInCompany(String company) {
        if (company == null || company.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be null or blank");
        }
        return employees.stream()
                .filter(e -> e.getCompany().equals(company))
                .mapToDouble(Employee::getSalary)
                .max()
                .orElse(0);
    }

    // Rozkład pracowników według statusu zatrudnienia
    public Map<String, Long> getEmployeeStatusDistribution() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                ));
    }

    // Identyfikacja pracownika z najwyższym wynagrodzeniem - operacja znajdowania maksimum z wykorzystaniem Optional do obsługi potencjalnie pustej kolekcji.
    public Optional<Employee> getEmployeeWithHighestSalary() {
        return employees.stream()
                .max(Comparator.comparing(Employee::getSalary));
    }

    // Zwraca listę pracowników z wynagrodzeniem niższym niż bazowa stawka ich stanowiska
    public List<Employee> validateSalaryConsistency() {
        return employees.stream()
                .filter(e -> e.getSalary() < e.getPosition().getBaseSalary())
                .toList();
    }

    // zwraca mapę, gdzie kluczem jest nazwa firmy, a wartością obiekt CompanyStatistics
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long count = list.size(); //liczba osób
                                    double avgSalary = list.stream()
                                            .mapToDouble(Employee::getSalary)
                                            .average()
                                            .orElse(0);

                                    String highestPaidEmployee = list.stream()
                                            .max(Comparator.comparing(Employee::getSalary))
                                            .map(e -> e.getFirstName() + " " + e.getLastName())
                                            .orElse("None");

                                    return new CompanyStatistics(count, avgSalary, highestPaidEmployee);
                                }
                        )
                ));
    }
}
