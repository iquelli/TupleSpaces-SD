package pt.ulisboa.tecnico.tuplespaces.sequencer;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;

public class SequencerServer {

    /** Server host port **/
    private static int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Logger.debug(SequencerServer.class.getSimpleName() + " started");

        // Print received arguments
        Logger.debug("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            Logger.debug("arg[%d] = %s%n", i, args[i]);
        }

        // Check arguments
        if (args.length != 0) {
            Logger.error("Too many arguments!");
            Logger.error("Usage: mvn exec:java");
            System.exit(1);
        }

        final BindableService impl = new SequencerServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(PORT).addService(impl).build();
        // Start the server
        server.start();
        // Server threads are running in the background
        Logger.info("Sequencer server started");

        // Do not exit the main thread
        // Wait until server is terminated
        server.awaitTermination();

        // Server is terminated
        Logger.info("Sequencer server stopped");
        server.shutdown();
    }

}
