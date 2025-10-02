package main.techcorp.model;

public enum Position {
    PRESIDENT(25000, 1),
    VICE_PRESIDENT(18000, 2),
    MANAGER(12000, 3),
    DEVELOPER(8000, 4),
    INTERN(3000, 5);

    private final double baseSalary;
    private final int hierarchyLevel;

    Position(double baseSalary, int hierarchyLevel){
        this.baseSalary = baseSalary;
        this.hierarchyLevel = hierarchyLevel;
    }
}
