package com.techcorp.employee.service;

import com.techcorp.employee.exception.InvalidDataException;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.ImportSummary;
import com.techcorp.employee.model.Position;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
public class ImportService {
    private final EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv(String path) {
        ImportSummary summary = new ImportSummary();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            // pominięcie nagłówka
            reader.readLine();
            lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Employee employee = parseEmployee(line);
                    boolean added = employeeService.addEmployee(employee);
                    if (added) {
                        summary.importedCount();
                    } else {
                        summary.addError(lineNumber, "Duplicate email: " + employee.getEmail());
                    }
                } catch (InvalidDataException e) {
                    summary.addError(lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            summary.addError(0, "Error reading file: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Importuje pracowników z pliku XML.
     * 
     * @param path ścieżka do pliku XML
     * @return podsumowanie importu
     */
    public ImportSummary importFromXml(String path) {
        ImportSummary summary = new ImportSummary();
        int employeeIndex = 0;

        try {
            File xmlFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList employeeNodes = doc.getElementsByTagName("employee");

            for (int i = 0; i < employeeNodes.getLength(); i++) {
                employeeIndex = i + 1;
                Node node = employeeNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        Element element = (Element) node;
                        Employee employee = parseXmlEmployee(element);
                        
                        boolean added = employeeService.addEmployee(employee);
                        if (added) {
                            summary.importedCount();
                        } else {
                            summary.addError(employeeIndex, "Duplicate email: " + employee.getEmail());
                        }
                    } catch (InvalidDataException | IllegalArgumentException e) {
                        summary.addError(employeeIndex, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            summary.addError(0, "Error reading XML file: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Parsuje element XML reprezentujący pracownika.
     */
    private Employee parseXmlEmployee(Element element) throws InvalidDataException {
        try {
            String firstName = getTagValue("firstName", element);
            String lastName = getTagValue("lastName", element);
            String email = getTagValue("email", element);
            String company = getTagValue("company", element);
            String positionStr = getTagValue("position", element);
            String salaryStr = getTagValue("salary", element);

            if (firstName == null || lastName == null || email == null || 
                company == null || positionStr == null || salaryStr == null) {
                throw new InvalidDataException("Missing required field in XML element");
            }

            Position position;
            try {
                position = Position.valueOf(positionStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Invalid position: " + positionStr);
            }

            double salary;
            try {
                salary = Double.parseDouble(salaryStr.trim());
                if (salary <= 0) {
                    throw new InvalidDataException("Salary must be positive");
                }
            } catch (NumberFormatException e) {
                throw new InvalidDataException("Invalid salary format: " + salaryStr);
            }

            return new Employee(firstName.trim(), lastName.trim(), email.trim(), 
                              company.trim(), position, salary);
        } catch (Exception e) {
            if (e instanceof InvalidDataException) {
                throw e;
            }
            throw new InvalidDataException("Error parsing employee from XML: " + e.getMessage());
        }
    }

    /**
     * Pomocnicza metoda do odczytywania wartości z elementu XML.
     */
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    private Employee parseEmployee(String line) throws InvalidDataException {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new InvalidDataException("Invalid number of fields in a file");
        }

        String firstName = parts[0].trim();
        String lastName = parts[1].trim();
        String email = parts[2].trim();
        String company = parts[3].trim();

        Position position;
        try {
            position = Position.valueOf(parts[4].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid position: " + parts[4].trim());
        }

        double salary;
        try {
            salary = Double.parseDouble(parts[5].trim());
            if (salary <= 0) {
                throw new InvalidDataException("Salary must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid salary format: " + parts[5].trim());
        }

        return new Employee(firstName, lastName, email, company, position, salary);
    }
}
