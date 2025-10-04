package main.techcorp.service;

import main.techcorp.model.Employee;
import main.techcorp.model.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmployeeService {
    boolean addEmployee(Employee employee);
    List<Employee> getAllEmployees();
    List<Employee> findEmployeesInCompany(String company);
    List<Employee> getEmployeesSortedByLastName();
    Map<Position, List<Employee>> getEmployeesGroupedByPosition();
    Map<Position, Long> countEmployeesOnPositions();
    double averageSalary();
    Optional<Employee> getEmployeeWithHighestSalary();
}
