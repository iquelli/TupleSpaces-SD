package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.ConnectionManager;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.TakeObserver;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse;

import java.util.List;

public class ClientService {

    private NameServerService nameServer;
    private SequencerService sequencer;
    private ConnectionManager connectionManager;
    private OrderedDelayer delayer;

    private ResponseCollector putCollector;
    private ResponseCollector readCollector;
    private ResponseCollector takeCollector;

    public ClientService(NameServerService nameServer, int numServers) {
        this.nameServer = nameServer;

        // Obtains new sequence numbers for put and take requests
        this.sequencer = new SequencerService();

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
        List<ManagedChannel> channels = nameServer.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        int seqNumber = sequencer.getSeqNumber();

        for (int id : delayer) {
            stubs.get(id)
                    .put(
                            PutRequest.newBuilder()
                                    .setNewTuple(newTuple)
                                    .setSeqNumber(seqNumber)
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
        List<ManagedChannel> channels = nameServer.getServersChannels();
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
        List<ManagedChannel> channels = nameServer.getServersChannels();
        List<TupleSpacesReplicaStub> stubs = connectionManager.resolveMultipleStubs(channels);
        int seqNumber = sequencer.getSeqNumber();

        for (int id : delayer) {
            stubs.get(id)
                    .take(
                            TakeRequest.newBuilder()
                                    .setSearchPattern(searchPattern)
                                    .setSeqNumber(seqNumber)
                                    .build(),
                            new TakeObserver(takeCollector)
                    );
        }

        // wait for all responses
        takeCollector.waitUntilAllReceived(3);
        String tuple = takeCollector.getResponse();
        takeCollector.clearResponses();
        connectionManager.closeChannels(channels);
        return tuple;
    }

    public List<String> getTupleSpacesState(String qualifier) throws StatusRuntimeException {
        ManagedChannel channel = nameServer.getChannel(qualifier);
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
