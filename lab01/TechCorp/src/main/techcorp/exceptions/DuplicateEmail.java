package main.techcorp.exceptions;

public class DuplicateEmail extends RuntimeException {
    public DuplicateEmail(String email) {
        super("Employee with email " + email + " already exists in the system.");
    }
}
