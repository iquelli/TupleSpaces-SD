package pt.ulisboa.tecnico.tuplespaces.server.exceptions;

/** Represents an exception thrown when a given pattern isn't matched. **/
public class InexistantTupleException extends RuntimeException {

    public InexistantTupleException(String input) {
        super(
                "The pattern '" + input +
                        "' doesn't match any tuple in the server."
        );
    }

}
