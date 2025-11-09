package com.techcorp.employee.dto;

import com.techcorp.employee.model.CompanyStatistics;

/**
 * Data Transfer Object dla statystyk firmy.
 * Reprezentuje zagregowane dane o pracownikach w firmie.
 */
public class CompanyStatisticsDTO {
    private String companyName;
    private Long employeeCount;
    private Double averageSalary;
    private Double highestSalary;
    private String topEarnerName;

    public CompanyStatisticsDTO() {
        // Domyślny konstruktor wymagany przez Jackson do deserializacji JSON
    }

    public CompanyStatisticsDTO(String companyName, Long employeeCount, Double averageSalary, 
                               Double highestSalary, String topEarnerName) {
        this.companyName = companyName;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestSalary = highestSalary;
        this.topEarnerName = topEarnerName;
    }

    /**
     * Konstruktor konwertujący model CompanyStatistics do DTO
     */
    public static CompanyStatisticsDTO fromCompanyStatistics(String companyName, 
                                                            CompanyStatistics stats, 
                                                            Double highestSalary) {
        return new CompanyStatisticsDTO(
            companyName,
            stats.getEmployeeCount(),
            stats.getAverageSalary(),
            highestSalary,
            stats.getHighestPaidEmployee()
        );
    }

    // Gettery i settery

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Long employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(Double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public Double getHighestSalary() {
        return highestSalary;
    }

    public void setHighestSalary(Double highestSalary) {
        this.highestSalary = highestSalary;
    }

    public String getTopEarnerName() {
        return topEarnerName;
    }

    public void setTopEarnerName(String topEarnerName) {
        this.topEarnerName = topEarnerName;
    }

    @Override
    public String toString() {
        return "CompanyStatisticsDTO{" +
                "companyName='" + companyName + '\'' +
                ", employeeCount=" + employeeCount +
                ", averageSalary=" + averageSalary +
                ", highestSalary=" + highestSalary +
                ", topEarnerName='" + topEarnerName + '\'' +
                '}';
    }
}
