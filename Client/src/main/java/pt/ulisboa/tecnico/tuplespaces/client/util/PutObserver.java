package pt.ulisboa.tecnico.tuplespaces.client.util;

import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.PutResponse;

public class PutObserver extends ResponseObserver<PutResponse> {

    public PutObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(PutResponse response) {
        collector.addResponse("OK");
        Logger.debug("Received response from put request");
    }

}
