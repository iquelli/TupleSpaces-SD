package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

import java.util.List;

public class ClientService extends TupleSpacesGrpc.TupleSpacesImplBase implements AutoCloseable {

    private final ManagedChannel channel;
    // Blocking stub for variant R1
    private final TupleSpacesGrpc.TupleSpacesBlockingStub stub;

    public ClientService(String host, String port) {
        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
        stub = TupleSpacesGrpc.newBlockingStub(channel);
    }

    public void put(String newTuple) throws StatusRuntimeException {
        stub.put(PutRequest.newBuilder().setNewTuple(newTuple).build());
    }

    public String read(String searchPattern) throws StatusRuntimeException {
        ReadResponse response = stub.read(
                ReadRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        TakeResponse response = stub.take(
                TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        // TODO: need name server logic to implement qualifier
        getTupleSpacesStateResponse response = stub.getTupleSpacesState(
                getTupleSpacesStateRequest.newBuilder().build()
        );

        return response.getTupleList();
    }

    @Override
    public void close() { // for autocloseable
        channel.shutdown();
    }

}
