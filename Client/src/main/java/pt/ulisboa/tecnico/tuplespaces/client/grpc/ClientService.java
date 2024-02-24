package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

/*
 * TODO: The gRPC client-side logic should be here.
 * This should include a method that builds a channel and stub,
 * as well as individual methods for each remote operation of this service.
 */

public class ClientService extends TupleSpacesGrpc.TupleSpacesImplBase implements AutoCloseable {

    private final ManagedChannel channel;
    private final TupleSpacesGrpc.TupleSpacesBlockingStub stub; // blocking stub for variant R1

    public ClientService(String host, String port) {
        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
        stub = TupleSpacesGrpc.newBlockingStub(channel);
    }

    public void put(String[] split) {
        // TODO
    }

    public void read(String[] split) {
        // TODO
    }

    public void take(String[] split) {
        // TODO
    }

    public void getTupleSpacesState(String[] split) {
        // TODO
    }

    @Override
    public void close() { // for autocloseable
        channel.shutdown();
    }

}
