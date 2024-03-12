package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;

import java.util.List;

public class TakeObserver extends ResponseObserver<TakePhase1Response> implements StreamObserver<TakePhase1Response> {

    public TakeObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(TakePhase1Response takeResponse) {
        List<String> tuples = takeResponse.getReservedTuplesList();

        if (collector.isEmpty()) {
            collector.addAllResponses(tuples);
        } else {
            collector.intersectResponses(tuples);
        }
        Logger.debug("Received response from take phase 1 request");
    }

}
