package pt.ulisboa.tecnico.tuplespaces.client.util;

import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.TakeResponse;

public class TakeObserver extends ResponseObserver<TakeResponse> {

    public TakeObserver(ResponseCollector collector) {
        super(collector);
    }

    @Override
    public void onNext(TakeResponse takeResponse) {
        collector.addResponse(takeResponse.getResult());
        Logger.debug("Received response from take request");
    }

}
