package za.ac.belgiumcampus.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Minimal contract that every CRUD-style DAO in the system follows.
 * Declaring it as a generic interface (rather than copy-pasting the same
 * four method signatures into every DAO class) is this module's example
 * of abstraction: calling code that only needs create/read/update/delete
 * can program against {@code GenericDAO<Material>} instead of the
 * concrete {@link MaterialDAO} class.
 *
 * @param <T>  the domain model this DAO manages (e.g. Material)
 * @author user
 */
public interface GenericDAO<T> {

    T add(T item) throws SQLException, za.ac.belgiumcampus.exception.ValidationException;

    T getById(int id) throws SQLException;

    List<T> getAll() throws SQLException;

    void update(T item) throws SQLException, za.ac.belgiumcampus.exception.ValidationException;

    void delete(int id) throws SQLException;
}
