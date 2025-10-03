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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String newFirstName) {
        this.firstName = newFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String newLastName) {
        this.lastName = newLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String newCompany) {
        this.company = newCompany;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double newSalary) {
        this.salary = newSalary;
    }

    // equals, hashCode, toString

    @Override
    public String toString() {
        return String.format("%s %s %s - %s at %s, Salary: %.2f",
                firstName, lastName, email, position, company, salary);
    }
}
