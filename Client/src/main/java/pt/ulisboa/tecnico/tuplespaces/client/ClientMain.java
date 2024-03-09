package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

import java.util.OptionalInt;

public class ClientMain {

    static final int numServers = 3;

    public static void main(String[] args) {
        Logger.debug("Hello Client!");

        // Check arguments
        if (args.length != 1) {
            Logger.error("Usage: mvn exec:java -Dexec.args=\"<client id>\"");
            System.exit(1);
        }

        final OptionalInt optClientID = parseClientID(args[0]);
        if (optClientID.isEmpty()) {
            Logger.error("The client id must be a not negative integer");
            Logger.error("Usage: mvn exec:java -Dexec.args=\"<client id>\"");
            System.exit(1);
        }
        final int clientID = optClientID.getAsInt();

        final NameServerService nameServerService = new NameServerService();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            nameServerService.close();
        }));

        CommandProcessor parser = new CommandProcessor(
                new ClientService(nameServerService, ClientMain.numServers, clientID)
        );
        parser.parseInput();
        nameServerService.close();
    }

    /**
     * Parses a string into a clientId number.
     *
     * @param strclientId A string of the clientId number.
     * @return An empty optional int in case the clientId is invalid, or an
     *         optional
     *         int of the clientId number.
     **/
    private static OptionalInt parseClientID(String strClientId) {
        try {
            final int clientId = Integer.parseInt(strClientId);
            if (clientId < 0) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(clientId);
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

}
