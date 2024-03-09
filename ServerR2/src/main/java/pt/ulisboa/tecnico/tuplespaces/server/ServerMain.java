package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.server.service.TupleSpacesReplicaXuLiskovServiceImpl;

import java.util.OptionalInt;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        Logger.debug(ServerMain.class.getSimpleName() + " started");

        // Receive and print arguments
        Logger.debug("Received %d arguments", args.length);
        for (int i = 0; i < args.length; i++) {
            Logger.debug("arg[%d] = %s", i, args[i]);
        }

        // Check number of arguments
        if (args.length != 2) {
            Logger.error("Wrong number of arguments!");
            Logger.error("Usage: mvn exec:java -Dexec.args=\"<port> <qualifier>\"");
            System.exit(1);
        }

        final OptionalInt optPort = parsePort(args[0]);
        if (optPort.isEmpty()) {
            Logger.error("The port number must be an integer between 1024 and 65535");
            Logger.error("Usage: mvn exec:java -Dexec.args=\"<port> <qualifier>\"");
            System.exit(1);
        }
        final int port = optPort.getAsInt();

        final String qualifier = args[1];
        if (qualifier == null || qualifier.isEmpty()) {
            Logger.error("The qualifier must be a non-empty string");
            Logger.error("Usage: mvn exec:java -Dexec.args=\"<port> <qualifier>\"");
            System.exit(1);
        }

        final NameServerService nameServerService = new NameServerService();

        final BindableService impl = new TupleSpacesReplicaXuLiskovServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(impl).build();

        try {
            // Server threads are running in the background.
            server.start();
        } catch (Exception e) {
            Logger.error("Failed to start server to listen on port %s", args[0]);
            System.exit(1);
        }

        try {
            nameServerService.register(port, qualifier);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                Logger.error("Name server is unreachable");
            } else {
                Logger.error(e.getStatus().getDescription());
            }
            System.exit(1);
        }

        Logger.info("TupleSpaces server has started%n");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("TupleSpaces server is shutting down");
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
