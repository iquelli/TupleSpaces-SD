package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.ConnectionManager;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse;

import java.util.List;

public class ClientService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    private NameServerService nameServerService;
    private ConnectionManager connectionManager;
    private OrderedDelayer delayer;

    private ResponseCollector putCollector;
    private ResponseCollector readCollector;
    private ResponseCollector takeCollector;

    public ClientService(NameServerService nameServerService, int numServers) {
        this.nameServerService = nameServerService;

        // Creates stubs, closes channels
        this.connectionManager = new ConnectionManager();

        /**
         * The delayer can be used to inject delays to the sending of requests to the
         * different servers, according to the per-server delays that have been set.
         */
        this.delayer = new OrderedDelayer(numServers);

        this.putCollector = new ResponseCollector();
        this.readCollector = new ResponseCollector();
        this.takeCollector = new ResponseCollector();
    }

    public void put(String newTuple) throws StatusRuntimeException, InterruptedException {
        // TODO: implement the client put
        // List<ManagedChannel> channels = nameServerService.getServersChannels();
        // List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        // for (int id : delayer) {
        //     stubs.get(id)
        //             .put(
        //                     PutRequest.newBuilder()
        //                             .setNewTuple(newTuple)
        //                             .build(),
        //                     new PutObserver(putCollector)
        //             );
        // }

        // // wait for all responses
        // putCollector.waitUntilAllReceived(3);
        // putCollector.clearResponses();
        // connectionManager.closeChannels(channels);
    }

    public String read(String searchPattern) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        readCollector.clearResponses();
        for (int id : delayer) {
            stubs.get(id)
                    .read(
                            ReadRequest.newBuilder()
                                    .setSearchPattern(searchPattern)
                                    .build(),
                            new ReadObserver(readCollector)
                    );
        }

        // wait for one response
        readCollector.waitUntilAllReceived(1);
        connectionManager.closeChannels(channels);
        return readCollector.getResponse();
    }

    public String take(String searchPattern) throws StatusRuntimeException, InterruptedException {
        // TODO: implement the client take
        // List<ManagedChannel> channels = nameServerService.getServersChannels();
        // List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        // int sleepTime = 0;

        // List<String> lockedTuples = new ArrayList<>();
        // while (true) {
        //     lockedTuples.addAll(takePhaseOne(stubs, searchPattern));
        //     takeCollector.clearResponses();

        //     // Given that we perform the intersection during response collection,
        //     // we can simply check this condition to determine if there are locked tuples.
        //     if (!lockedTuples.isEmpty()) {
        //         break; // got a tuple, can exit phase 1
        //     }

        //     // Send release request
        //     takePhaseOneRelease(stubs);

        //     // Sleep for a random amount of time that caps at 60 seconds before sending
        //     // a new request
        //     if (sleepTime < 60 * 1000) {
        //         sleepTime += 5 * 1000;
        //     }
        // }

        // // Initialize Take phase 2
        // String selectedTuple = lockedTuples.get(0);
        // takePhaseTwo(stubs, selectedTuple);

        // connectionManager.closeChannels(channels);
        // return selectedTuple;
        return null;
    }

    public List<String> getTupleSpacesState(String qualifier) throws StatusRuntimeException {
        ManagedChannel channel = nameServerService.getChannel(qualifier);
        // blocking stub for this operation
        TupleSpacesReplicaBlockingStub stub = connectionManager.resolveBlockingStub(channel);

        getTupleSpacesStateResponse response = stub.getTupleSpacesState(
                getTupleSpacesStateRequest.newBuilder().build()
        );

        connectionManager.closeChannel(channel);
        return response.getTupleList();
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
