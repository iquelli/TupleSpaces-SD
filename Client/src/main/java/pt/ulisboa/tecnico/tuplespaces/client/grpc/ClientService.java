package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseObserver;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;




import java.util.List;

public class ClientService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    NameServerService nameServerService;
    OrderedDelayer delayer;
    ResponseCollector collector;

    public ClientService(NameServerService nameServerService, int numServers) {
        this.nameServerService = nameServerService;

        /* The delayer can be used to inject delays to the sending of requests to the
           different servers, according to the per-server delays that have been set  */
        this.delayer = new OrderedDelayer(numServers);
    }

    public void put(String newTuple) throws StatusRuntimeException, InterruptedException {
        List<TupleSpacesReplicaStub> stubs = nameServerService.connectToServers();
        for (int id : delayer) {
            stubs.get(id).put(PutRequest.newBuilder().setNewTuple(newTuple).build(), new ResponseObserver(collector));
        }
        collector.waitUntilAllPutReceived(3);
    }

    public String read(String searchPattern) throws StatusRuntimeException, InterruptedException {
        List<TupleSpacesReplicaStub> stubs = nameServerService.connectToServers();
        for (int id : delayer) {
            stubs.get(id).read(ReadRequest.newBuilder().setSearchPattern(searchPattern).build(), new ResponseObserver(collector));
        }
        collector.waitUntilAllReadReceived(1);

        return collector.getReadStrings();
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        List<TupleSpacesReplicaStub> stubs = nameServerService.connectToServers();
        for (int id : delayer) {
            // TakeResponse response = stubs[id].take(
            //         TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
            // );
            // TODO: adjust take for multiple servers
        }

        // return response.getResult();
        return null;
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        List<TupleSpacesReplicaStub> stubs = nameServerService.connectToServers();
        for (int id : delayer) {
            // getTupleSpacesStateResponse response = stubs[id].getTupleSpacesState(
            //         getTupleSpacesStateRequest.newBuilder().build()
            // );
            // TODO: adjust getTupleSpacesState for multiple servers
        }

        // return response.getTupleList();
        return null;
    }

    /**
     * This method allows the command processor to set the request delay
     * assigned to a given server.
     *
     * Example:
     * How to use the delayer before sending requests to each server.
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
