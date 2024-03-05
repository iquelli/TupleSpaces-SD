package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.common.grpc.NameServerService;

import java.util.List;

public class ClientService extends TupleSpacesGrpc.TupleSpacesImplBase {

    NameServerService nameServerService;
    // For commands that don't specify a qualifier
    // The empty qualifier will return all servers available
    String defaultQualifier = "";
    OrderedDelayer delayer;

    public ClientService(NameServerService nameServerService, int numServers) {
        this.nameServerService = nameServerService;

        /* The delayer can be used to inject delays to the sending of requests to the
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
    }

    public void put(String newTuple) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(defaultQualifier);
        stub.put(PutRequest.newBuilder().setNewTuple(newTuple).build());
    }

    public String read(String searchPattern) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(defaultQualifier);
        ReadResponse response = stub.read(
                ReadRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public String take(String searchPattern) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(defaultQualifier);
        TakeResponse response = stub.take(
                TakeRequest.newBuilder().setSearchPattern(searchPattern).build()
        );

        return response.getResult();
    }

    public List<String> getTupleSpacesState(
            String qualifier
    ) throws StatusRuntimeException {
        TupleSpacesBlockingStub stub = nameServerService.connectToServer(qualifier);
        getTupleSpacesStateResponse response = stub.getTupleSpacesState(
                getTupleSpacesStateRequest.newBuilder().build()
        );

        return response.getTupleList();
    }

    /* This method allows the command processor to set the request delay assigned to
        a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);

        /* TODO: Remove this debug snippet */

        // System.out.println("[Debug only]: After setting the delay, I'll test it");
        // for (Integer i : delayer) {
        //   System.out.println("[Debug only]: Now I can send request to stub[" + i + "]");
        // }
        // System.out.println("[Debug only]: Done.");

        /* Example: How to use the delayer before sending requests to each server
                    Before entering each iteration of this loop, the delayer has already
                    slept for the delay associated with server indexed by 'id'.
                    id is in the range 0..(numServers-1).
        
           for (Integer id : delayer) {
               stub[id].some_remote_method(some_arguments);
           }
        */
    }

}
