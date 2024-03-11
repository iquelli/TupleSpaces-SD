package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ResponseObserverRead implements StreamObserver<ReadResponse> {

    ResponseCollector collector;

    public ResponseObserverRead(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(ReadResponse readResponse) {
        collector.addResponse(readResponse.getResult());
        Logger.debug("Received response from read request");
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