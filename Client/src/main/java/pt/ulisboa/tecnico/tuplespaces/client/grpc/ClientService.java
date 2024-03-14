package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.ConnectionManager;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.TakeObserver;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientService extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    private final int ID;
    private NameServerService nameServerService;
    private ConnectionManager connectionManager;
    private OrderedDelayer delayer;

    private ResponseCollector putCollector;
    private ResponseCollector readCollector;
    private ResponseCollector takeCollector;

    public ClientService(NameServerService nameServerService, int numServers, int id) {
        this.ID = id;
        this.nameServerService = nameServerService;

        // Creates stubs, closes channels
        this.connectionManager = new ConnectionManager();

        /*
         * The delayer can be used to inject delays to the sending of requests to the
         * different servers, according to the per-server delays that have been set
         */
        this.delayer = new OrderedDelayer(numServers);

        this.putCollector = new ResponseCollector();
        this.readCollector = new ResponseCollector();
        this.takeCollector = new ResponseCollector();
    }

    public void put(String newTuple) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        for (int id : delayer) {
            stubs.get(id)
                    .put(
                            PutRequest.newBuilder()
                                    .setNewTuple(newTuple)
                                    .build(),
                            new PutObserver(putCollector)
                    );
        }

        // wait for all responses
        putCollector.waitUntilAllReceived(3);
        putCollector.clearResponses();
        connectionManager.closeChannels(channels);
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
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        Random random = new Random(this.ID);
        int sleepTime = 0;

        List<String> lockedTuples = new ArrayList<>();
        while (true) {
            lockedTuples.addAll(takePhaseOne(stubs, searchPattern));
            takeCollector.clearResponses();
            if (!lockedTuples.isEmpty()) {
                break; // got a tuple, can exit phase 1
            }

            // Send release request
            takePhaseOneRelease(stubs);
            // Sleep for a random amount of time that caps at 60 seconds before sending
            // a new request
            if (sleepTime < 60 * 1000) {
                sleepTime += 5 * 1000;
            }
            Thread.sleep(random.nextInt(sleepTime));
        }

        // Initialize Take phase 2
        String selectedTuple = lockedTuples.get(0);
        takePhaseTwo(stubs, selectedTuple);

        connectionManager.closeChannels(channels);
        return selectedTuple;
    }

    public List<String> takePhaseOne(
            List<TupleSpacesReplicaStub> stubs,
            String searchPattern
    ) throws StatusRuntimeException, InterruptedException {

        for (int id : delayer) {
            stubs.get(id)
                    .takePhase1(
                            TakePhase1Request.newBuilder()
                                    .setSearchPattern(searchPattern)
                                    .setClientId(this.ID)
                                    .build(),
                            new TakeObserver(takeCollector)
                    );
        }

        // wait for all responses
        takeCollector.waitUntilAllReceived(3);
        return takeCollector.getResponses();
    }

    public void takePhaseOneRelease(
            List<TupleSpacesReplicaStub> stubs
    ) throws StatusRuntimeException, InterruptedException {

        for (int id : delayer) {
            stubs.get(id)
                    .takePhase1Release(
                            TakePhase1ReleaseRequest.newBuilder()
                                    .setClientId(this.ID)
                                    .build(),
                            new ResponseObserver<TakePhase1ReleaseResponse>(takeCollector)
                    );
        }

        // wait for all responses
        takeCollector.waitUntilAllReceived(3);
        takeCollector.clearResponses();
    }

    public void takePhaseTwo(
            List<TupleSpacesReplicaStub> stubs,
            String selectedTuple
    ) throws StatusRuntimeException, InterruptedException {

        for (int id : delayer) {
            stubs.get(id)
                    .takePhase2(
                            TakePhase2Request.newBuilder()
                                    .setTuple(selectedTuple)
                                    .setClientId(this.ID)
                                    .build(),
                            new ResponseObserver<TakePhase2Response>(takeCollector)
                    );
        }

        // wait for all responses
        takeCollector.waitUntilAllReceived(3);
        takeCollector.clearResponses();
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
