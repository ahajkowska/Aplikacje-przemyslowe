package com.techcorp.employee;

import com.techcorp.employee.model.Employee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class Main {

    private final List<Employee> xmlEmployees;

    public Main(@Qualifier("xmlEmployees") List<Employee> xmlEmployees) {
        this.xmlEmployees = xmlEmployees;
    }

    public List<Employee> getXmlEmployees() {
        return xmlEmployees;
    }
}