package dk.library.db;

/** Unchecked indpakning af SQLException, så API-lagene ikke skal håndtere checked exceptions. */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
