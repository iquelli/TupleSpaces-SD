package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

public class ClientMain {

    static final int numServers = 3;

    public static void main(String[] args) {
        Logger.debug("Hello Client!");

        // Print received arguments
        Logger.debug("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            Logger.debug("arg[%d] = %s%n", i, args[i]);
        }

        // Check that the client receives no arguments
        if (args.length != 0) {
            Logger.error("Too many arguments!");
            Logger.error("Usage: mvn exec:java");
            System.exit(1);
        }

        final NameServerService nameServer = new NameServerService();

        // create hook for ctrl+c
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            nameServer.close();
        }));

        // start up client
        CommandProcessor parser = new CommandProcessor(
                new ClientService(nameServer, ClientMain.numServers)
        );
        parser.parseInput();
        nameServer.close();
    }

}
