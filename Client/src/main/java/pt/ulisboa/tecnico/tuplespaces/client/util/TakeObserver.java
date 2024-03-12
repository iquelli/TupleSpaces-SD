package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;

import java.util.List;
import java.util.stream.Collectors;

public class TakeObserver extends ResponseObserver<TakePhase1Response> implements StreamObserver<TakePhase1Response> {

    public TakeObserver(ResponseCollector collector) {
        super(collector);
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

}
