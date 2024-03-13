package pt.ulisboa.tecnico.tuplespaces.client.util;

import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ReadObserver extends ResponseObserver<ReadResponse> {

    public ReadObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(ReadResponse readResponse) {
        collector.addResponse(readResponse.getResult());
        Logger.debug("Received response from read request");
    }

}
