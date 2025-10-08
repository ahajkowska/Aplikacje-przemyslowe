package main.techcorp.model;

import java.util.Objects;

public class Employee {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String company;
    private final Position position;
    private double salary;

    // konstruktor z bazową pensją
    public Employee(String firstName, String lastName, String email, String company, Position position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = position.getBaseSalary();
    }

    // konstruktor z wybraną pensją
    public Employee(String firstName, String lastName, String email, String company, Position position, double salary) {
        this(firstName, lastName, email, company, position);
        this.salary = salary;
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

    // equals, hashCode, toString
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
                ", salary: " + salary;
    }
}
