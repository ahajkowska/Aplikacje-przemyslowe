package com.techcorp.employee.model;

import java.util.ArrayList;
import java.util.List;

public class ImportSummary {
    private int importedCount; // liczba zaimportowanych pracowników
    private List<String> errors;

    public ImportSummary() {
        this.importedCount = 0;
        this.errors = new ArrayList<>();
    }

    public void importedCount() {
        this.importedCount++;
    }

    public void addError(int lineNumber, String message) {
        this.errors.add("Line " + lineNumber + ": " + message);
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ImportSummary | " +
                "importedCount=" + importedCount +
                ", errors=" + errors;
    }
}
