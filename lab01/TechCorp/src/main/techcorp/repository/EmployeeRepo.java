package main.techcorp.repository;

import main.techcorp.model.Employee;

import java.util.ArrayList;
import java.util.List;

// przechowywanie pracownikow

public class EmployeeRepo {
    private final List<Employee> employees = new ArrayList<>();

    // Dodawanie nowego pracownika do systemu z walidacją unikalności adresu email przed dodaniem
    // Wyświetlanie listy wszystkich pracowników w systemie
}
