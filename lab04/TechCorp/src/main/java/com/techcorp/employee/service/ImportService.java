package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
public class ImportService {
    private final EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv(String path) {
        ImportSummary summary = new ImportSummary();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            // pominięcie nagłówka
            reader.readLine();
            lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Employee employee = parseEmployee(line);
                    boolean added = employeeService.addEmployee(employee);
                    if (added) {
                        summary.importedCount();
                    } else {
                        summary.addError(lineNumber, "Duplicate email: " + employee.getEmail());
                    }
                } catch (InvalidDataException e) {
                    summary.addError(lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            summary.addError(0, "Error reading file: " + e.getMessage());
        }

        return summary;
    }

    private Employee parseEmployee(String line) throws InvalidDataException {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new InvalidDataException("Invalid number of fields in a file");
        }

        String firstName = parts[0].trim();
        String lastName = parts[1].trim();
        String email = parts[2].trim();
        String company = parts[3].trim();

        Position position;
        try {
            position = Position.valueOf(parts[4].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid position: " + parts[4].trim());
        }

        double salary;
        try {
            salary = Double.parseDouble(parts[5].trim());
            if (salary <= 0) {
                throw new InvalidDataException("Salary must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid salary format: " + parts[5].trim());
        }

        return new Employee(firstName, lastName, email, company, position, salary);
    }
}
