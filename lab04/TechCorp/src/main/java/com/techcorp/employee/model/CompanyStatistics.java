package com.techcorp.employee.model;

//zawierający liczbę pracowników w firmie, średnie wynagrodzenie oraz pełne imię i nazwisko osoby z najwyższym wynagrodzeniem
public class CompanyStatistics {
    private long employeeCount;
    private double averageSalary;
    private String highestPaidEmployee;

    public CompanyStatistics(long employeeCount, double averageSalary, String highestPaidEmployee) {
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestPaidEmployee = highestPaidEmployee;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public String getHighestPaidEmployee() {
        return highestPaidEmployee;
    }

    @Override
    public String toString() {
        return "CompanyStatistics | " +
                "employeeCount:" + employeeCount +
                ", averageSalary:" + averageSalary +
                ", highestPaidEmployee:'" + highestPaidEmployee;
    }
}
