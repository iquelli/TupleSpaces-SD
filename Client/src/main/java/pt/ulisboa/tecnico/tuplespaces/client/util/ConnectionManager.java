package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    public ConnectionManager() {

    }

    public TupleSpacesReplicaStub resolveStub(ManagedChannel channel) {
        TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channel);
        Logger.debug("Connected to server");
        return stub;
    }

    public TupleSpacesReplicaBlockingStub resolveBlockingStub(ManagedChannel channel) {
        TupleSpacesReplicaBlockingStub stub = TupleSpacesReplicaGrpc.newBlockingStub(channel);
        Logger.debug("Connected to server");
        return stub;
    }

    public void closeChannel(ManagedChannel channel) {
        channel.shutdown();
    }

    public void closeChannels(List<ManagedChannel> channels) {
        for (ManagedChannel channel : channels) {
            closeChannel(channel);
        }
    }

    public List<TupleSpacesReplicaStub> resolveMultipleStubs(List<ManagedChannel> channels) {
        List<TupleSpacesReplicaStub> stubs = new ArrayList<TupleSpacesReplicaStub>();
        for (ManagedChannel channel : channels) {
            stubs.add(resolveStub(channel));
        }
        return stubs;
    }

}
