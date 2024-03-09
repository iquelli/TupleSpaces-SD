package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;

import java.util.List;

public class ClientService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    NameServerService nameServerService;
    // For commands that don't specify a qualifier
    // The empty qualifier will return all servers available
    String defaultQualifier = "";
    OrderedDelayer delayer;

    public ClientService(NameServerService nameServerService, int numServers) {
        this.nameServerService = nameServerService;

        /* The delayer can be used to inject delays to the sending of requests to the
           different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
    }

    public void put(String newTuple) throws StatusRuntimeException {
        TupleSpacesReplicaStub stub = nameServerService.connectToServer(defaultQualifier);
        // stub.put(PutRequest.newBuilder().setNewTuple(newTuple).build());
        // TODO: adjust put for multiple servers
    }

    public String read(String searchPattern) throws StatusRuntimeException {
        TupleSpacesReplicaStub stub = nameServerService.connectToServer(defaultQualifier);
        // ReadResponse response = stub.read(
        //         ReadRequest.newBuilder().setSearchPattern(searchPattern).build()
        // );
        // TODO: adjust read for multiple servers

        // return response.getResult();
        return null;
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        TupleSpacesReplicaStub stub = nameServerService.connectToServer(defaultQualifier);
        // TakeResponse response = stub.take(
        //         TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
        // );
        // TODO: adjust take for multiple servers

        // return response.getResult();
        return null;
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        TupleSpacesReplicaStub stub = nameServerService.connectToServer(qualifier);
        // getTupleSpacesStateResponse response = stub.getTupleSpacesState(
        //         getTupleSpacesStateRequest.newBuilder().build()
        // );
        // TODO: adjust getTupleSpacesState for multiple servers

        // return response.getTupleList();
        return null;
    }

    /**
     * This method allows the command processor to set the request delay
     * assigned to
     * a given server.
     *
     * Example: How to use the delayer before sending requests to each server.
     * Before entering each iteration of this loop, the delayer has already
     * slept for the delay associated with server indexed by 'id'.
     * - id is in the range 0..(numServers-1).
     *
     * for (Integer id : delayer) {
     * stub[id].some_remote_method(some_arguments);
     * }
     */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

}
