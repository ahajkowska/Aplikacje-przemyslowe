package com.techcorp.employee.model;

import java.util.Objects;

public class Employee {
    private String firstName;
    private String lastName;
    private final String email;
    private final String company;
    private final Position position;
    private double salary;
    private EmploymentStatus status;
    private String photoFileName;  // Nazwa pliku ze zdjęciem profilowym

    // konstruktor z bazową pensją
    public Employee(String firstName, String lastName, String email, String company, Position position) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name cannot be null or blank");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name cannot be null or blank");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");
        if (company == null || company.isBlank())
            throw new IllegalArgumentException("Company cannot be null or blank");
        if (position == null)
            throw new IllegalArgumentException("Position cannot be null");

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = position.getBaseSalary();
        this.status = EmploymentStatus.ACTIVE; // Domyślny status
    }

    // konstruktor z wybraną pensją
    public Employee(String firstName, String lastName, String email, String company, Position position, double salary) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name cannot be null or blank");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name cannot be null or blank");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");
        if (company == null || company.isBlank())
            throw new IllegalArgumentException("Company cannot be null or blank");
        if (position == null)
            throw new IllegalArgumentException("Position cannot be null");
        if (salary < 0)
            throw new IllegalArgumentException("Salary cannot be negative");

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = EmploymentStatus.ACTIVE; // Domyślny status
    }

    // konstruktor z wybraną pensją i statusem
    public Employee(String firstName, String lastName, String email, String company, Position position, double salary, EmploymentStatus status) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name cannot be null or blank");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name cannot be null or blank");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be null or blank");
        if (company == null || company.isBlank())
            throw new IllegalArgumentException("Company cannot be null or blank");
        if (position == null)
            throw new IllegalArgumentException("Position cannot be null");
        if (salary < 0)
            throw new IllegalArgumentException("Salary cannot be negative");
        if (status == null)
            throw new IllegalArgumentException("Status cannot be null");

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public Position getPosition() {
        return position;
    }

    public double getSalary() {
        return salary;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Employee e) && Objects.equals(email, e.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Employee | " +
                firstName + " " +
                lastName +
                ", email: " + email +
                ", company: " + company +
                ", position: " + position +
                ", salary: " + salary +
                ", status: " + status;
    }
}
