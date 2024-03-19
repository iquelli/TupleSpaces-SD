package pt.ulisboa.tecnico.tuplespaces.server.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.List;

/**
 * Implements the TupleSpaces Replica Total Order Variant service, handling gRPC
 * requests.
 **/
public class TupleSpacesReplicaTotalOrderServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    private final ServerState state;

    public TupleSpacesReplicaTotalOrderServiceImpl() {
        state = new ServerState();
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        // TODO: implement put service
        // try {
        //     Logger.debug("[INFO] Received PUT request:%n%s", request);
        //     state.put(request.getNewTuple());

        //     // Use responseObserver to send a single response back
        //     responseObserver.onNext(PutResponse.getDefaultInstance());
        //     responseObserver.onCompleted();
        // } catch (InvalidTupleException e) {
        //     Logger.debug("[ERR] PUT operation failed: %s", e.getMessage());
        //     responseObserver.onError(
        //             Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
        //     );
        // } catch (RuntimeException e) {
        //     Logger.debug("[ERR] PUT operation failed: %s", e.getMessage());
        //     responseObserver.onError(
        //             Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
        //     );
        // }
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        try {
            Logger.debug("[INFO] Received READ request:%n%s", request);
            String tuple = state.read(request.getSearchPattern());

            // Builder to construct a new Protobuffer object
            ReadResponse response = ReadResponse.newBuilder().setResult(tuple).build();

            // Use responseObserver to send a single response back
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidTupleException e) {
            Logger.debug("[ERR] READ operation failed: %s", e.getMessage());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (InterruptedException e) {
            Logger.debug("[ERR] READ operation failed: %s", e.getMessage());
            responseObserver.onError(
                    Status.CANCELLED.withDescription(
                            "Client interrupted while waiting for tuple: " + e.getMessage()
                    ).asRuntimeException()
            );
        } catch (RuntimeException e) {
            Logger.debug("[ERR] READ operation failed: %s", e.getMessage());
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        // TODO: implement take service
        // try {
        //     Logger.debug("[INFO] Received TAKE_PHASE_1 request:%n%s", request);
        //     List<String> tuples = state.lock(request.getSearchPattern(), request.getClientId());

        //     // Builder to construct a new Protobuffer object
        //     TakePhase1Response response = TakePhase1Response.newBuilder()
        //             .addAllReservedTuples(tuples)
        //             .build();

        //     // Use responseObserver to send a single response back
        //     responseObserver.onNext(response);
        //     responseObserver.onCompleted();
        // } catch (InvalidClientIDException e) {
        //     Logger.debug("[ERR] TAKE_PHASE_1 operation failed: %s", e.getMessage());
        //     responseObserver.onError(
        //             Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
        //     );
        // } catch (InvalidTupleException e) {
        //     Logger.debug("[ERR] TAKE_PHASE_1 operation failed: %s", e.getMessage());
        //     responseObserver.onError(
        //             Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
        //     );
        // } catch (RuntimeException e) {
        //     Logger.debug("[ERR] TAKE_PHASE_1 operation failed: %s", e.getMessage());
        //     responseObserver.onError(
        //             Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
        //     );
        // }
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
            Logger.debug("[ERR] GET_TUPLE_SPACE_STATE operation failed: %s", e.getMessage());
            responseObserver.onError(
                    Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

}
