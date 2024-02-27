package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerAddress;

public class ClientMain {

    public static void main(String[] args) {
        System.out.println(ClientMain.class.getSimpleName());

        // Check arguments
        if (args.length != 0) {
            System.err.println("Too many arguments!");
            System.err.println("Usage: mvn exec:java");
            System.exit(1);
        }

        final NameServerService nameServerService = new NameServerService();
        // There is only one server now, therefore empty qualifier will give us
        // the only server available
        String qualifier = "";

        try {
            ServerAddress server = nameServerService.lookup(qualifier);
            // Named servers not implemented yet
            try (var clientService = new ClientService(
                    server.getHost(),
                    Integer.toString(server.getPort())
            )) {
                CommandProcessor parser = new CommandProcessor(clientService);
                parser.parseInput();
            }
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                System.out.println("Failed to estabilish connection to server");
            } else if (e.getStatus().getDescription() != null) {
                System.out.println(e.getStatus().getDescription());
            } else {
                System.err.println("gRCP Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println(
                    "An unexpected error occurred: " + e.getMessage()
            );
        }
        nameServerService.close();
    }

}
