package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.ConnectionManager;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseObserver;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;

import java.util.List;

public class ClientService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    NameServerService nameServerService;
    OrderedDelayer delayer;
    ResponseCollector collector = new ResponseCollector();
    ConnectionManager connectionManager;

    public ClientService(NameServerService nameServerService, int numServers) {
        this.nameServerService = nameServerService;

        /* The delayer can be used to inject delays to the sending of requests to the
           different servers, according to the per-server delays that have been set  */
        this.delayer = new OrderedDelayer(numServers);
        this.connectionManager = new ConnectionManager();
    }

    public void put(String newTuple) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        for (int id : delayer) {
            stubs.get(id)
                    .put(
                            PutRequest.newBuilder().setNewTuple(newTuple).build(),
                            new ResponseObserver(collector)
                    );
        }
        collector.waitUntilAllPutReceived(3);
        connectionManager.closeChannels(channels);
    }

    public String read(String searchPattern) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        for (int id : delayer) {
            stubs.get(id)
                    .read(
                            ReadRequest.newBuilder().setSearchPattern(searchPattern).build(),
                            new ResponseObserver(collector)
                    );
        }
        collector.waitUntilAllReadReceived(1);
        connectionManager.closeChannels(channels);
        return collector.getReadStrings();
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        for (int id : delayer) {
            // TakeResponse response = stubs[id].take(
            //         TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
            // );
            // TODO: adjust take for multiple servers
        }

        connectionManager.closeChannels(channels);
        // return response.getResult();
        return null;
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        ManagedChannel channel = nameServerService.getChannel(qualifier);
        TupleSpacesReplicaBlockingStub stub = connectionManager.resolveBlockingStub(channel);

        for (int id : delayer) {
            // getTupleSpacesStateResponse response = stubs[id].getTupleSpacesState(
            //         getTupleSpacesStateRequest.newBuilder().build()
            // );
            // TODO: adjust getTupleSpacesState for multiple servers
        }

        // return response.getTupleList();
        connectionManager.closeChannel(channel);
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
