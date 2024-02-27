package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

import java.util.List;

public class ClientService extends TupleSpacesGrpc.TupleSpacesImplBase implements AutoCloseable {

    NameServerService nameServerService;
    // For commands that don't specify a qualifier
    // The empty qualifier will return all servers available
    String defaultQualifier = "";

    public ClientService(NameServerService nameServerService) {
        this.nameServerService = nameServerService;
    }

    public void put(String newTuple) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(
                defaultQualifier
        );
        stub.put(PutRequest.newBuilder().setNewTuple(newTuple).build());
    }

    public String read(String searchPattern) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(
                defaultQualifier
        );
        ReadResponse response = stub.read(
                ReadRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(
                defaultQualifier
        );
        TakeResponse response = stub.take(
                TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(
                qualifier
        );
        getTupleSpacesStateResponse response = stub.getTupleSpacesState(
                getTupleSpacesStateRequest.newBuilder().build()
        );

        return response.getTupleList();
    }

    @Override
    public void close() { // for autocloseable
    }

}
