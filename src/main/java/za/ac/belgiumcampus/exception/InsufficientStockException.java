package za.ac.belgiumcampus.exception;

public class InsufficientStockException extends ValidationException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
