package main.techcorp;

import main.techcorp.model.Employee;
import main.techcorp.model.Position;
import main.techcorp.service.TechSystem;

public class Main {
    public static void main(String[] args) {
        TechSystem system = new TechSystem();

        //zarządzanie pracownikami
        system.addEmployee(new Employee("Adam", "Małysz", "adam.malysz@gmail.com", "TechCorp", Position.PRESIDENT, 30000));
        system.addEmployee(new Employee("Harry", "Potter", "harry.potter@techcorp.eu", "TechCorp", Position.DEVELOPER, 15000));
        system.addEmployee(new Employee("Magda", "Gessler", "magda.gessler@gmail.com", "MiniCorp", Position.INTERN));

        // Testing duplicate email
        boolean added = system.addEmployee(new Employee("Duplicate", "Email", "harry.potter@techcorp.eu",
                "TechCorp", Position.DEVELOPER, 8000));
        System.out.println("Employee with duplicate email added: " + added);

        System.out.println("\n--- All Employees ---");
        system.getAllEmployees().forEach(System.out::println);

        //operacje analityczne
        String chosenCompany = "TechCorp";
        System.out.println("\n--- Employees working at " + chosenCompany + " ---");
        system.findEmployeesInCompany(chosenCompany).forEach(System.out::println);

        System.out.println("\n--- Employees sorted by last name ---");
        system.getEmployeesSortedByLastName().forEach(
                e -> System.out.println(e.getLastName() + ", " + e.getFirstName())
        );

        System.out.println("\n--- Employees grouped by position ---");
        system.getEmployeesGroupedByPosition().forEach((position, employeeList) -> {
            System.out.println(position + ": ");
            employeeList.forEach(e -> System.out.println("  - " + e.getFirstName() + " " + e.getLastName()));
        });

        System.out.println("\n--- Number of employees by position ---");
        system.countEmployeesOnPositions().forEach((position, count) -> {
            System.out.println(position + ": " + count);
        });

        //statystyki finansowe
        System.out.println("\n--- Average salary ---");
        System.out.println("Average salary: " + system.averageSalary());

        System.out.println("\n--- Employee with highest salary ---");
        system.getEmployeeWithHighestSalary().ifPresent(e ->
                System.out.println(e.getFirstName() + " " + e.getLastName() + ": " + e.getSalary()));
    }
}