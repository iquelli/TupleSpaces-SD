package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;


import java.util.List;
import java.util.stream.Collectors;



public class ResponseObserverTake<T> implements StreamObserver<T> {

    ResponseCollector collector;

    public ResponseObserverTake(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(T takeResponse) {
        if (takeResponse instanceof TakePhase1Response) {
            List<String> tuples = ((TakePhase1Response) takeResponse).getReservedTuplesList();

            if (collector.isEmpty()) collector.setResponses(tuples);

            else {
                collector.setResponses(
                        tuples.stream()
                                .filter(tuple -> collector.getResponses().contains(tuple))
                                .collect(Collectors.toList())
                );
            }
            Logger.debug("Received response from take phase 1 request");
        }
        else if (takeResponse instanceof TakePhase1ReleaseResponse) {
            //collector.addResponse("OK");
            Logger.debug("Received response from take phase 1 (release) request");
        }
        else if (takeResponse instanceof TakePhase2Response) {
            //collector.addResponse("OK");
            Logger.debug("Received response from take phase 2 request");
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