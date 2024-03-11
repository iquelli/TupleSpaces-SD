package pt.ulisboa.tecnico.tuplespaces.server.exceptions;

/** Represents an exception thrown when an invalid client ID is read. **/
public class InvalidClientIDException extends RuntimeException {

    public InvalidClientIDException(int clientId) {
        super(
                "The client identifier '" + clientId +
                        "' is invalid, it must be a positive integer."
        );
    }

}
