package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

import java.util.OptionalInt;
import java.util.Random;

public class ClientMain {

    static final int numServers = 3;

    public static void main(String[] args) {
        Logger.debug("Hello Client!");

        if (args.length > 1) {
            Logger.error("Usage: mvn exec:java < -Dexec.args=\"<(optional) client id>\" >");
            System.exit(1);
        }

        // Check arguments. If no specified id, generate random one
        final OptionalInt optClientID = (args.length != 0)
                ? parseClientID(args[0])
                : findRandomClientID();

        if (optClientID.isEmpty()) {
            Logger.error("The client id must be a not negative integer");
            Logger.error("Usage: mvn exec:java < -Dexec.args=\"<(optional) client id>\" >");
            System.exit(1);
        }

        final int clientID = optClientID.getAsInt();

        final NameServerService nameServerService = new NameServerService();

        // create hook for ctrl+c
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            nameServerService.close();
        }));

        // start up client
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

    /**
     * Creates a random clientId number.
     *
     * @return An empty optional int of a randomly generated clientId
     **/
    private static OptionalInt findRandomClientID() {
        long currentTimeMillis = System.currentTimeMillis();
        long nanoTime = System.nanoTime();
        long seed = currentTimeMillis ^ nanoTime;
        Random random = new Random(seed);

        return OptionalInt.of(random.nextInt(Integer.MAX_VALUE));
    }

}
