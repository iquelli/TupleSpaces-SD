package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

/**
 * Implements the TupleSpaces Centralized Variant service, handling gRPC
 * requests.
 **/
public class TupleSpacesCentralizedServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase {

    private final ServerState state;

    public TupleSpacesCentralizedServiceImpl() {
        state = new ServerState();
    }

    @Override
    public void put(
            PutRequest request,
            StreamObserver<PutResponse> responseObserver
    ) {
        // TODO: put service
    }

    @Override
    public void read(
            ReadRequest request,
            StreamObserver<ReadResponse> responseObserver
    ) {
        // TODO: read service
    }

    @Override
    public void take(
            TakeRequest request,
            StreamObserver<TakeResponse> responseObserver
    ) {
        // TODO: take service
    }

    @Override
    public void getTupleSpacesState(
            getTupleSpacesStateRequest request,
            StreamObserver<getTupleSpacesStateResponse> responseObserver
    ) {
        // TODO: getTupleSpacesState service
    }

}
