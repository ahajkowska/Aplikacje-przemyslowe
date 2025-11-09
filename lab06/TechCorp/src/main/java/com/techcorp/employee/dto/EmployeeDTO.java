package com.techcorp.employee.dto;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;

/**
 * Data Transfer Object dla pracownika.
 * Uniwersalna reprezentacja pracownika w API używana we wszystkich operacjach (GET, POST, PUT).
 */
public class EmployeeDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private String position;
    private Double salary;
    private String status;

    public EmployeeDTO() {
        // Domyślny konstruktor wymagany przez Jackson do deserializacji JSON
    }

    public EmployeeDTO(String firstName, String lastName, String email, String company, 
                      String position, Double salary, String status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    /**
     * Konstruktor konwertujący model Employee do DTO
     */
    public static EmployeeDTO fromEmployee(Employee employee) {
        return new EmployeeDTO(
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            employee.getCompany(),
            employee.getPosition().name(),
            employee.getSalary(),
            employee.getStatus() != null ? employee.getStatus().name() : "ACTIVE"
        );
    }

    /**
     * Konwersja DTO do modelu Employee
     */
    public Employee toEmployee() {
        Position pos = Position.valueOf(position.toUpperCase());
        com.techcorp.employee.model.EmploymentStatus empStatus = status != null 
            ? com.techcorp.employee.model.EmploymentStatus.valueOf(status.toUpperCase())
            : com.techcorp.employee.model.EmploymentStatus.ACTIVE;
        return new Employee(firstName, lastName, email, company, pos, salary, empStatus);
    }

    // Gettery i settery

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "EmployeeDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", position='" + position + '\'' +
                ", salary=" + salary +
                ", status='" + status + '\'' +
                '}';
    }
}
