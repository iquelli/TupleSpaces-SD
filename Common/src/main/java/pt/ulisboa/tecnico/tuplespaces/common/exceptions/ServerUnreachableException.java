package pt.ulisboa.tecnico.tuplespaces.common.exceptions;

public class ServerUnreachableException extends Exception {

    public ServerUnreachableException(String qualifier) {
        super(
                String.format(
                        "Cannot resolve server with qualifier '%s'",
                        qualifier
                )
        );
    }

}
