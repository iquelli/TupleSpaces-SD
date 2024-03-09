package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ResponseObserver<T> implements StreamObserver<T> {

    ResponseCollector collector;

    public ResponseObserver(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(T response) {
        if (response instanceof ReadResponse) {
            ReadResponse readResponse = (ReadResponse) response;
            // Now you can work with typedResponse
            collector.addResponse(readResponse.getResult());
            Logger.debug("Received response from read request");
        } else if (response instanceof PutResponse) {
            // Handle other cases or log a message
            collector.addResponse("OK");
            Logger.debug("Received response from put request");
        } else {
            Logger.error("Received unexpected response type: " + response.getClass());
        }
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
