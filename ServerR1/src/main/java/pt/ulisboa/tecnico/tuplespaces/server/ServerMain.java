package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.server.service.TupleSpacesCentralizedServiceImpl;

import java.util.OptionalInt;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        // TODO: add proper logging
        System.out.println(ServerMain.class.getSimpleName());

        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check number of arguments
        if (args.length != 2) {
            System.err.println("Wrong number of arguments!");
            System.err.println(
                    "Usage: mvn exec:java -Dexec.args=\"<port> <qualifier>\""
            );
            System.exit(1);
        }

        final OptionalInt optPort = parsePort(args[0]);
        if (optPort.isEmpty()) {
            System.err.println(
                    "The port number must be an integer between 1024 and 65535"
            );
            System.exit(1);
        }
        final int port = optPort.getAsInt();

        final String qualifier = args[1];
        if (qualifier == null || qualifier.isEmpty() ||
                (!qualifier.equals("A") && !qualifier.equals("B") && !qualifier
                        .equals("C"))) {
            System.err.println(
                    "The qualifier must be a non-empty string, that is either A, B or C"
            );
            System.exit(1);
        }

        final NameServerService nameServerService = new NameServerService();

        final BindableService impl = new TupleSpacesCentralizedServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(impl).build();

        server.start();
        // Server threads are running in the background.

        try {
            nameServerService.register(port, qualifier);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                System.out.println("Name server is unreachable");
            } else {
                System.out.println(e.getStatus().getDescription());
            }
            System.exit(1);
        }

        System.out.println("TupleSpaces server has started");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("TupleSpaces server is shutting down");
            nameServerService.delete(port);
            nameServerService.close();
        }));

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }

    /**
     * Parses a string into a port number (1024 to 65535).
     *
     * @param strPort A string of the port number.
     * @return An empty optional int in case the port is invalid, or an optional
     *         int of the port number.
     **/
    private static OptionalInt parsePort(String strPort) {
        try {
            final int port = Integer.parseInt(strPort);
            if (port < 1024 || port > 65535) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(port);
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

}
