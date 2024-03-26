package pt.ulisboa.tecnico.tuplespaces.sequencer;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc.SequencerImplBase;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberRequest;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberResponse;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;

public class SequencerServiceImpl extends SequencerImplBase {

    int seqNumber;

    public SequencerServiceImpl() {
        seqNumber = 0;
    }

    @Override
    synchronized public void getSeqNumber(
            GetSeqNumberRequest request,
            StreamObserver<GetSeqNumberResponse> responseObserver
    ) {
        Logger.debug("[INFO] Received GET_SEQ_NUMBER request:");
        seqNumber++;
        Logger.debug("The sequence number returned is: %d", seqNumber);
        GetSeqNumberResponse response = GetSeqNumberResponse.newBuilder()
                .setSeqNumber(seqNumber)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
