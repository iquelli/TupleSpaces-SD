package pt.ulisboa.tecnico.tuplespaces.server.exceptions;

/** Represents an exception thrown when no locked tuple by the client with client ID is found. **/
public class TupleNotFoundException extends RuntimeException {

    public TupleNotFoundException(String input, int clientId) {
        super(
                "No tuple with the format '" + input +
                        "' is locked by the client with identifier '" + clientId + "'."
        );
    }

}
