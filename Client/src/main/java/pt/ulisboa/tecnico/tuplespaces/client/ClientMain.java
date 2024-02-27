package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

public class ClientMain {

    public static void main(String[] args) {
        System.out.println("Hello Client!");

        // Check arguments
        if (args.length != 0) {
            Logger.error("Too many arguments!");
            Logger.error("Usage: mvn exec:java");
            System.exit(1);
        }

        final NameServerService nameServerService = new NameServerService();

        try (var clientService = new ClientService(nameServerService)) {
            CommandProcessor parser = new CommandProcessor(clientService);
            parser.parseInput();
        }

        nameServerService.close();
    }

}
