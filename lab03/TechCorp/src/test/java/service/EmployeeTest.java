package service;

import model.Employee;
import model.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testCreateEmployeeWithValidData() {
        Employee e = new Employee("Harry", "Potter", "harry@firma.com", "TechCorp", Position.DEVELOPER, 5000);
        assertEquals("Harry", e.getFirstName());
        assertEquals("Potter", e.getLastName());
        assertEquals("harry@firma.com", e.getEmail());
        assertEquals("TechCorp", e.getCompany());
        assertEquals(Position.DEVELOPER, e.getPosition());
        assertEquals(5000, e.getSalary());
    }

    @Test
    void testThrowExceptionWhenFirstNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee(null, "Potter", "harry@corp.com", "TechCorp", Position.DEVELOPER, 5000)
        );
    }

    @Test
    void testThrowExceptionWhenSalaryIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Harry", "Potter", "harry@corp.com", "TechCorp", Position.DEVELOPER, -100)
        );
    }

    @Test
    void testThrowExceptionWhenPositionIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Harry", "Potter", "harry@corp.com", "TechCorp", null, 4000)
        );
    }

    @Test
    void testThrowExceptionWhenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee("Harry", "Potter", "   ", "TechCorp", Position.DEVELOPER, 4000)
        );
    }

    @Test
    void testEmployeesWithSameEmailAreEqual() {
        Employee e1 = new Employee("Michael", "Jordan", "email@firma.com", "Firma", Position.DEVELOPER, 9000);
        Employee e2 = new Employee("Hannah", "Montana", "email@firma.com", "Firma", Position.DEVELOPER, 9000);

        assertEquals(e1, e2, "Pracownicy z tym samym emailem powinni być równi");
    }

    @Test
    void testEmployeesWithDifferentEmailsAreNotEqual() {
        Employee e1 = new Employee("Michael", "Jordan", "michael@firma.com", "Firma", Position.DEVELOPER, 9000);
        Employee e2 = new Employee("Hannah", "Montana", "hannah@firma.com", "Firma", Position.DEVELOPER, 9000);

        assertNotEquals(e1, e2, "Pracownicy z różnymi emailami nie powinni być równi");
    }

    @Test
    void testHashCodeIsEqualForSameEmail() {
        Employee e1 = new Employee("Michael", "Jordan", "email@firma.com", "Firma", Position.DEVELOPER, 9000);
        Employee e2 = new Employee("Hannah", "Montana", "email@firma.com", "Firma", Position.DEVELOPER, 9000);

        assertEquals(e1.hashCode(), e2.hashCode(), "hashCode powinien być taki sam dla pracowników z tym samym emailem");
    }

    @Test
    void testToStringContainsKeyInformation() {
        Employee e = new Employee("Michael", "Jordan", "michael@firma.com", "Firma", Position.DEVELOPER, 9000);
        String s = e.toString();
        assertTrue(s.contains("Michael"));
        assertTrue(s.contains("Jordan"));
        assertTrue(s.contains("michael@firma.com"));
        assertTrue(s.contains("Firma"));
        assertTrue(s.contains("DEVELOPER"));
        assertTrue(s.contains("9000"));
    }

    @Test
    void testConstructorSetsSalaryFromBaseSalary() {
        Employee e = new Employee("Michael", "Jordan", "michael@firma.com", "Firma", Position.MANAGER);
        assertEquals(Position.MANAGER.getBaseSalary(), e.getSalary(), "Pensja powinna być ustawiona na bazową z Position");
    }

    @Test
    void testConstructorWithSalaryOverridesBaseSalary() {
        Employee e = new Employee("Michael", "Jordan", "michael@firma.com", "Firma", Position.MANAGER, 15000);
        assertEquals(15000, e.getSalary(), "Pensja powinna być ustawiona na podaną wartość, a nie na bazową");
    }
}