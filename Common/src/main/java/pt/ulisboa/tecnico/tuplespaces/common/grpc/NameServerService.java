package pt.ulisboa.tecnico.tuplespaces.common.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.common.exceptions.ServerUnreachableException;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.DeleteRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.RegisterRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerAddress;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerInfo;


public class NameServerService implements AutoCloseable {

    private static final String NAME_SERVER_HOST = "localhost";
    private static final int NAME_SERVER_PORT = 5001;
    private static final String SERVICE_NAME = "TupleSpaces";

    private final ManagedChannel channel;
    private final NameServerGrpc.NameServerBlockingStub stub;

    public NameServerService() {
        channel = ManagedChannelBuilder.forAddress(
                NAME_SERVER_HOST,
                NAME_SERVER_PORT
        ).usePlaintext().build();
        stub = NameServerGrpc.newBlockingStub(channel);
    }

    public void register(int port, String qualifier) {
        stub.register(
                RegisterRequest.newBuilder()
                        .setServiceName(SERVICE_NAME)
                        .setAddress(
                                ServerAddress.newBuilder()
                                        .setHost(NAME_SERVER_HOST)
                                        .setPort(port)
                                        .build()
                        )
                        .setQualifier(qualifier)
                        .build()
        );
    }

    public ServerAddress lookup(
            String qualifier
    ) throws ServerUnreachableException {
        LookupResponse response = stub.lookup(
                LookupRequest.newBuilder()
                        .setServiceName(SERVICE_NAME)
                        .setQualifier(qualifier)
                        .build()
        );

        // get list of servers
        return response.getServerList()
                .stream()
                .findFirst()
                .map(ServerInfo::getAddress)
                .orElseThrow(() -> new ServerUnreachableException(qualifier));
    }

    public void delete(int port) {
        stub.delete(
                DeleteRequest.newBuilder()
                        .setServiceName(SERVICE_NAME)
                        .setAddress(
                                ServerAddress.newBuilder()
                                        .setHost(NAME_SERVER_HOST)
                                        .setPort(port)
                                        .build()
                        )
                        .build()
        );
    }

    @Override
    public void close() {
        channel.shutdown();
    }

}
