package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    public ConnectionManager() {
        // do nothing
    }

    /**
     * Resolves a non blocking stub from a certain channel.
     */
    public TupleSpacesReplicaStub resolveStub(ManagedChannel channel) {
        TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channel);
        Logger.debug("Connected to server using non-blocking stub");
        return stub;
    }

    /**
     * Resolves non blocking stubs from a list of channels.
     */
    public List<TupleSpacesReplicaStub> resolveMultipleStubs(List<ManagedChannel> channels) {
        List<TupleSpacesReplicaStub> stubs = new ArrayList<TupleSpacesReplicaStub>();
        for (ManagedChannel channel : channels) {
            stubs.add(resolveStub(channel));
        }
        return stubs;
    }

    /**
     * Resolves a blocking stub from a certain channel.
     */
    public TupleSpacesReplicaBlockingStub resolveBlockingStub(ManagedChannel channel) {
        TupleSpacesReplicaBlockingStub stub = TupleSpacesReplicaGrpc.newBlockingStub(channel);
        Logger.debug("Connected to server using blocking stub");
        return stub;
    }

    /**
     * Closes a channel.
     */
    public void closeChannel(ManagedChannel channel) {
        channel.shutdown();
    }

    /**
     * Closes a list of channels.
     */
    public void closeChannels(List<ManagedChannel> channels) {
        for (ManagedChannel channel : channels) {
            closeChannel(channel);
        }
    }

}
