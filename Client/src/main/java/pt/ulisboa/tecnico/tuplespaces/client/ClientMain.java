package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.exceptions.ServerUnreachableException;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerAddress;

public class ClientMain {

    public static void main(String[] args) {
        System.out.println(ClientMain.class.getSimpleName());

        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check arguments
        if (args.length != 0) {
            System.err.println("Too many arguments!");
            System.err.println(
                    "Usage: mvn exec:java"
            );
            System.exit(1);
        }
        
        final NameServerService nameServerService = new NameServerService();
        // There is only one server now
        String qualifier = ""; // so, no qualifier will give us the only server available
        
        try {
            ServerAddress server = nameServerService.lookup(qualifier);
            // Named servers not implemented yet
            try (var clientService = new ClientService(server.getHost(), Integer.toString(server.getPort()))) {
                CommandProcessor parser = new CommandProcessor(clientService);
                parser.parseInput();
            }
        } catch (ServerUnreachableException e) {
            System.err.println("Could not resolve a server!");
            nameServerService.close();
        }

    }
}
