package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberRequest;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberResponse;

public class SequencerService {

    private static final String SEQUENCER_SERVER_HOST = "localhost";
    private static final int SEQUENCER_SERVER_PORT = 8080;

    private final ManagedChannel channel;
    private final SequencerGrpc.SequencerBlockingStub stub;

    public SequencerService() {
        channel = ManagedChannelBuilder.forAddress(SEQUENCER_SERVER_HOST, SEQUENCER_SERVER_PORT)
                .usePlaintext()
                .build();
        stub = SequencerGrpc.newBlockingStub(channel);
    }

    public int getSeqNumber() {
        GetSeqNumberResponse response = stub.getSeqNumber(GetSeqNumberRequest.newBuilder().build());
        return response.getSeqNumber();
    }

}
