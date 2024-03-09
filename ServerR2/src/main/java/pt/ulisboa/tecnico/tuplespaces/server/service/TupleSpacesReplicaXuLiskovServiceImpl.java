package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.List;

/**
 * Implements the TupleSpaces Replica Xu Liskov Variant service, handling gRPC
 * requests.
 **/
public class TupleSpacesReplicaXuLiskovServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    private final ServerState state;

    public TupleSpacesReplicaXuLiskovServiceImpl() {
        state = new ServerState();
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            Logger.debug("[INFO] Received PUT request:%n%s", request);
            state.put(request.getNewTuple());

            // Use responseObserver to send a single response back
            responseObserver.onNext(PutResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            Logger.debug("[ERR] PUT operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (RuntimeException e) {
            Logger.debug("[ERR] PUT operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        try {
            Logger.debug("[INFO] Received READ request:%n%s", request);
            String tuple = state.read(request.getSearchPattern());
            ReadResponse response = ReadResponse.newBuilder().setResult(tuple).build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            Logger.debug("[ERR] READ operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (InterruptedException e) {
            Logger.debug("[ERR] READ operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.CANCELLED.withDescription(
                            "Client interrupted while waiting for tuple: " + e.getMessage()
                    ).asRuntimeException()
            );
        } catch (RuntimeException e) {
            Logger.debug("[ERR] READ operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void takePhase1(
            TakePhase1Request request,
            StreamObserver<TakePhase1Response> responseObserver
    ) {
        // TODO: take phase 1 service
    }

    @Override
    public void takePhase1Release(
            TakePhase1ReleaseRequest request,
            StreamObserver<TakePhase1ReleaseResponse> responseObserver
    ) {
        // TODO: take phase 1 release service
    }

    @Override
    public void takePhase2(
            TakePhase2Request request,
            StreamObserver<TakePhase2Response> responseObserver
    ) {
        // TODO: take phase 2 service
    }

    @Override
    public void getTupleSpacesState(
            getTupleSpacesStateRequest request,
            StreamObserver<getTupleSpacesStateResponse> responseObserver
    ) {
        try {
            Logger.debug("[INFO] Received GET_TUPLE_SPACE_STATE request%n", request);
            // Get the tuples from the space state
            List<String> tuples = state.getTupleSpacesState();

            // Builder to construct a new Protobuffer object
            getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder()
                    .addAllTuple(tuples)
                    .build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            Logger.debug("[ERR] GET_TUPLE_SPACE_STATE operation failed:%s", e.getMessage());
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

}
