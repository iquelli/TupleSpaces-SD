package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;

public class PutObserver implements StreamObserver<PutResponse> {

    ResponseCollector collector;

    public PutObserver(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(PutResponse response) {
        collector.addResponse("OK");
        Logger.debug("Received response from put request");
    }

    @Override
    public void onError(Throwable throwable) {
        Logger.error("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        Logger.debug("Request completed");
    }

}
