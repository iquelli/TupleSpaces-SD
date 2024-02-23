package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.tuplespaces.server.service.TupleSpacesCentralizedServiceImpl;

import java.io.IOException;

public class ServerMain {

    public static void main(
            String[] args
    ) throws IOException, InterruptedException {
        // Receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check number of arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println(
                    "Usage: mvn exec:java -Dexec.args=<port> <qualifier>"
            );
            return;
        }

        final int port = Integer.parseInt(args[0]);

        // Check argument validity
        if (port <= 0 || port >= 65536) {
            System.err.println(
                    "Invalid port number, it should be between 1 and 65535."
            );
            return;
        }

        final BindableService impl = new TupleSpacesCentralizedServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(impl).build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("TupleSpaces server has started!");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }

}
