package service;

import model.Employee;
import model.Position;
import service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TechSystemTest {
    private EmployeeService techSystem;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        techSystem = new EmployeeService();

        employee1 = new Employee("Adam", "Małysz", "adam.malysz@gmail.com", "TechCorp", Position.DEVELOPER, 30000);
        employee2 = new Employee("Harry", "Potter", "harry.potter@techcorp.eu", "TechCorp", Position.DEVELOPER, 15000);
        employee3 = new Employee("Magda", "Gessler", "magda.gessler@gmail.com", "MiniCorp", Position.MANAGER, 3000);

        techSystem.addEmployee(employee1);
        techSystem.addEmployee(employee2);
        techSystem.addEmployee(employee3);
    }

    @Test
    void testAddEmployee() {
        Employee newEmployee = new Employee("Kylian", "Mbappe", "kylian.mbappe@techcorp.com", "TechCorp", Position.INTERN, 3500);
        assertTrue(techSystem.addEmployee(newEmployee), "Should add an employee");
    }

    @Test
    void testNotAddDuplicateEmail() {
        boolean added = techSystem.addEmployee(new Employee("Test", "User", "harry.potter@techcorp.eu", "TechCorp", Position.DEVELOPER, 12000));
        assertFalse(added, "Should not allow duplicate emails");
    }

    @Test
    void testThrowExceptionWhenAddingNullEmployee() {
        assertThrows(IllegalArgumentException.class, () -> techSystem.addEmployee(null));
    }

    @Test
    void testGetAllEmployees() {
        List<Employee> allEmployees = techSystem.getAllEmployees();
        assertEquals(3, allEmployees.size());
        assertTrue(allEmployees.contains(employee1));
        assertTrue(allEmployees.contains(employee2));
        assertTrue(allEmployees.contains(employee3));
    }

    @Test
    void testFindEmployeesInCompany() {
        List<Employee> employees = techSystem.findEmployeesInCompany("TechCorp");
        assertEquals(2, employees.size());
        assertTrue(employees.stream().allMatch(e -> e.getCompany().equals("TechCorp")));
    }

    @Test
    void testThrowExceptionWhenCompanyNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> techSystem.findEmployeesInCompany("  "));
    }

    @Test
    void testGetEmployeesSortedByLastName() {
        List<Employee> sorted = techSystem.getEmployeesSortedByLastName();
        assertEquals("Gessler", sorted.get(0).getLastName());
        assertEquals("Małysz", sorted.get(1).getLastName());
        assertEquals("Potter", sorted.get(2).getLastName());
    }

    @Test
    void testGetEmployeesGroupedByPosition() {
        Map<Position, List<Employee>> groupedEmployees = techSystem.getEmployeesGroupedByPosition();
        assertEquals(2, groupedEmployees.get(Position.DEVELOPER).size());
        assertEquals(1, groupedEmployees.get(Position.MANAGER).size());
        assertNull(groupedEmployees.get(Position.PRESIDENT)); // Nie ma pracowników na tym stanowisku
    }

    @Test
    void testCountEmployeesOnPositions() {
        Map<Position, Long> countByPosition = techSystem.countEmployeesOnPositions();
        assertEquals(2, countByPosition.get(Position.DEVELOPER));
        assertEquals(1, countByPosition.get(Position.MANAGER));
        assertNull(countByPosition.get(Position.PRESIDENT)); // Nie ma pracowników na tym stanowisku
    }

    @Test
    void testAverageSalary() {
        double average = techSystem.averageSalary();
        double expected = (employee1.getSalary() + employee2.getSalary() + employee3.getSalary()) / 3.0;
        assertEquals(expected, average);
    }

    @Test
    void testGetEmployeeWithHighestSalary() {
        Optional<Employee> highestPaid = techSystem.getEmployeeWithHighestSalary();
        assertTrue(highestPaid.isPresent());
        assertEquals(employee1, highestPaid.get());
    }
}