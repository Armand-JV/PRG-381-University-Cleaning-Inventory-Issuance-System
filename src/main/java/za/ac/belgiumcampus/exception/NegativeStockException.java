package za.ac.belgiumcampus.exception;

/**
 * Thrown whenever an operation would leave a material's quantity below
 * zero - e.g. reducing stock by more than is currently available, or
 * saving a material with a negative quantity/reorder level typed directly
 * into the form.
 *
 * Kept as its own class (rather than a generic ValidationException) so
 * calling code can catch it separately if it ever needs to react
 * differently to "would go negative" versus "field left blank" -type
 * failures - a small, deliberate use of exception-type inheritance.
 *
 * @author user
 */
public class NegativeStockException extends ValidationException {

    public NegativeStockException(String message) {
        super(message);
    }
}
