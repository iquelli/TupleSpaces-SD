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
        this.connectionManager = new ConnectionManager();

        /*
         * The delayer can be used to inject delays to the sending of requests to the
         * different servers, according to the per-server delays that have been set
         */
        this.delayer = new OrderedDelayer(numServers);
    }

    public void put(String newTuple) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        for (int id : delayer) {
            stubs.get(id)
                    .put(
                            PutRequest.newBuilder().setNewTuple(newTuple).build(),
                            new PutObserver(putCollector)
                    );
        }
        putCollector.waitUntilAllReceived(3);
        connectionManager.closeChannels(channels);
    }

    public String read(String searchPattern) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        for (int id : delayer) {
            stubs.get(id)
                    .read(
                            ReadRequest.newBuilder().setSearchPattern(searchPattern).build(),
                            new ReadObserver(readCollector)
                    );
        }
        readCollector.waitUntilAllReceived(1);
        connectionManager.closeChannels(channels);
        return readCollector.getResponse();
    }

    public String take(String searchPattern) throws StatusRuntimeException, InterruptedException {
        List<ManagedChannel> channels = nameServerService.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);

        List<String> phaseOneResponses;
        while (true) {
            phaseOneResponses = takePhaseOne(stubs, searchPattern);

            if (!phaseOneResponses.isEmpty())
                break; // got a tuple

            // Send release request
            takePhaseOneRelease(stubs);
        }

        // pick a tuple
        String selectedTuple = getRandomEntry(phaseOneResponses);

        // Initialize Take phase 2
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

        takeCollector.waitUntilAllReceived(3);
        return takeCollector.getResponses();
    }

    public void takePhaseOneRelease(
            List<TupleSpacesReplicaStub> stubs
    ) throws StatusRuntimeException, InterruptedException {
        for (int id : delayer) {
            stubs.get(id)
                    .takePhase1Release(
                            TakePhase1ReleaseRequest.newBuilder().setClientId(this.ID).build(),
                            new ResponseObserver<TakePhase1ReleaseResponse>(takeCollector)
                    );
        }

        takeCollector.waitUntilAllReceived(3);
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

        takeCollector.waitUntilAllReceived(3);
    }

    public List<String> getTupleSpacesState(String qualifier) throws StatusRuntimeException {
        ManagedChannel channel = nameServerService.getChannel(qualifier);
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

    /*
     * This method allows to get a random entry from a list of strings.
     */
    private static String getRandomEntry(List<String> list) {
        Random random = new Random();
        int index = random.nextInt(list.size());
        return list.get(index);
    }

}
