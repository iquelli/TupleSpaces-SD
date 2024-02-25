package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.Status;
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
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InexistantTupleException;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.List;

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
        try {
            state.put(request.getNewTuple());

            // Use responseObserver to send a single response back
            responseObserver.onNext(PutResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void read(
            ReadRequest request,
            StreamObserver<ReadResponse> responseObserver
    ) {
        try {
            String tuple = state.read(request.getSearchPattern());
            ReadResponse response = ReadResponse.newBuilder()
                    .setResult(tuple)
                    .build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (InexistantTupleException e) {
            responseObserver.onError(
                    Status.UNAVAILABLE.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void take(
            TakeRequest request,
            StreamObserver<TakeResponse> responseObserver
    ) {
        try {
            String tuple = state.take(request.getSearchPattern());
            TakeResponse response = TakeResponse.newBuilder()
                    .setResult(tuple)
                    .build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (InexistantTupleException e) {
            responseObserver.onError(
                    Status.UNAVAILABLE.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getTupleSpacesState(
            getTupleSpacesStateRequest request,
            StreamObserver<getTupleSpacesStateResponse> responseObserver
    ) {
        try {
            // Get the tuples from the space state
            List<String> tuples = state.getTupleSpacesState();

            // Builder to construct a new Protobuffer object
            getTupleSpacesStateResponse response = getTupleSpacesStateResponse
                    .newBuilder()
                    .addAllTuple(tuples)
                    .build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage())
                            .asRuntimeException()
            );
        }
    }

}
