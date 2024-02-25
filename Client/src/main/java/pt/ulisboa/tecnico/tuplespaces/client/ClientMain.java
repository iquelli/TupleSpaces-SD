package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {

    public static void main(String[] args) {
        System.out.println(ClientMain.class.getSimpleName());

        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println(
                    "Usage: mvn exec:java -Dexec.args=<host> <port>"
            );
            return;
        }

        // Get the host and the port
        final String host = args[0];
        final String port = args[1];

        // Check port validity
        if (Integer.parseInt(port) <= 0 || Integer.parseInt(port) >= 65536) {
            System.err.println(
                    "Invalid port number, it should be between 1 and 65535."
            );
            return;
        }

        // Named servers not implemented yet
        try (var clientService = new ClientService(host, port)) {
            CommandProcessor parser = new CommandProcessor(clientService);
            parser.parseInput();
        }
    }

}
