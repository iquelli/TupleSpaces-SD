package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;

public class PutObserver extends ResponseObserver<PutResponse> implements StreamObserver<PutResponse> {

    public PutObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(PutResponse response) {
        collector.addResponse("OK");
        Logger.debug("Received response from put request");
    }

}
