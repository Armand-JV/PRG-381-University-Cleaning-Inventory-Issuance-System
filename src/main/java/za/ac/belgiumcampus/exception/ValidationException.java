package za.ac.belgiumcampus.exception;

/**
 * Thrown when data entered by a user fails a business validation rule
 * before it ever reaches the database (required fields, non-negative
 * quantities, malformed numbers, etc.).
 *
 * This is a checked exception on purpose: the DAO/controller layer must
 * decide how to react (usually: show a JOptionPane and let the user fix
 * the field), so callers are forced to handle it rather than let it
 * silently propagate.
 *
 * {@link NegativeStockException} extends this class to represent the more
 * specific "would result in negative stock" rule, demonstrating exception
 * inheritance alongside the project's OOP requirements.
 *
 * @author user
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
