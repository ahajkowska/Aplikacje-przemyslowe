package main.techcorp.service;

import main.techcorp.model.Employee;
import main.techcorp.model.Position;

import java.util.*;
import java.util.stream.Collectors;

public class TechSystem implements EmployeeService {

    private final Set<Employee> employees;

    public TechSystem() {
        this.employees = new HashSet<>();
    }

    // Dodawanie nowego pracownika do systemu z walidacją unikalności adresu email przed dodaniem
    public boolean addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employees.stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(employee.getEmail()))) {
            return false;
        }
        return employees.add(employee);
    }

    // Wyświetlanie listy wszystkich pracowników w systemie
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
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

    // Identyfikacja pracownika z najwyższym wynagrodzeniem - operacja znajdowania maksimum z wykorzystaniem Optional do obsługi potencjalnie pustej kolekcji.
    public Optional<Employee> getEmployeeWithHighestSalary() {
        return employees.stream()
                .max(Comparator.comparing(Employee::getSalary));
    }
}
