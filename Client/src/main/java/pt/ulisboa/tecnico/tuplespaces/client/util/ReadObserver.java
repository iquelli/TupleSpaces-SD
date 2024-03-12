package pt.ulisboa.tecnico.tuplespaces.client.util;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ReadObserver extends ResponseObserver<ReadResponse> implements StreamObserver<ReadResponse> {

    public ReadObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(ReadResponse readResponse) {
        collector.addResponse(readResponse.getResult());
        Logger.debug("Received response from read request");
    }

}
