package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;

import java.util.List;
import java.util.stream.Collectors;

public class TakeObserver implements StreamObserver<TakePhase1Response> {

    ResponseCollector collector;

    public TakeObserver(ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(TakePhase1Response takeResponse) {
        List<String> tuples = takeResponse.getReservedTuplesList();

        if (collector.isEmpty()) {
            collector.setResponses(tuples);
        } else {
            collector.setResponses(
                    tuples.stream()
                            .filter(tuple -> collector.getResponses().contains(tuple))
                            .collect(Collectors.toList())
            );
        }
        Logger.debug("Received response from take phase 1 request");
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
