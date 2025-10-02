package main.techcorp.model;

public class Employee {
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private Position position;
    private double salary;

    public Employee(String firstName, String lastName, String email, String company, Position position, double salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
    }

    // equals, hashCode

    @Override
    public String toString() {
        return String.format("%s %s %s - %s at %s, Salary: %.2f",
                firstName, lastName, email, position, company, salary);
    }
}
