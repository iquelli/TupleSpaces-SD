package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;

public class ClientService extends TupleSpacesGrpc.TupleSpacesImplBase implements AutoCloseable {

    private final ManagedChannel channel;
    private final TupleSpacesGrpc.TupleSpacesBlockingStub stub; // blocking stub for variant R1

    public ClientService(String host, String port) {
        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
        stub = TupleSpacesGrpc.newBlockingStub(channel);
    }

    public void put(String newTuple) throws StatusRuntimeException {
        stub.put(PutRequest.newBuilder().setNewTuple(newTuple).build());
    }

    public void read(String searchPattern) throws StatusRuntimeException {
        stub.read(ReadRequest.newBuilder().setSearchPattern(searchPattern).build());
    }

    public void take(String searchPattern) throws StatusRuntimeException {
        stub.take(TakeRequest.newBuilder().setSearchPattern(searchPattern).build());
    }

    public void getTupleSpacesState(String qualifier) throws StatusRuntimeException {
        // TODO IMPLEMENT - need name server logic to implement qualifier
        stub.getTupleSpacesState(getTupleSpacesStateRequest.newBuilder().build());
    }

    @Override
    public void close() { // for autocloseable
        channel.shutdown();
    }

}
