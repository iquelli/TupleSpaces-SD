package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;

public class ReleaseObserver<T> implements StreamObserver<T> {

    ResponseCollector collector;

    public ReleaseObserver(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(T takeResponse) {
        collector.addResponse("OK");
        Logger.debug("Received response from take phase 1 (release) request");
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
