package pt.ulisboa.tecnico.tuplespaces.common.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.DeleteRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.RegisterRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class NameServerService implements AutoCloseable {

    private static final String NAME_SERVER_HOST = "localhost";
    private static final int NAME_SERVER_PORT = 5001;
    private static final String SERVICE_NAME = "TupleSpaces";
    private static final String[] QUALIFIERS = {"A", "B", "C"};

    private final ManagedChannel channel;
    private final NameServerGrpc.NameServerBlockingStub stub;

    public NameServerService() {
        channel = ManagedChannelBuilder.forAddress(NAME_SERVER_HOST, NAME_SERVER_PORT)
                .usePlaintext()
                .build();
        stub = NameServerGrpc.newBlockingStub(channel);
    }

    public void register(int port, String qualifier) throws StatusRuntimeException {
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

    public void delete(int port) {
        try {
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
        } catch (StatusRuntimeException e) {
            Logger.debug("[ERR] %s", e.getMessage());
        }
    }

    public ServerAddress lookup(String qualifier) throws StatusRuntimeException {
        LookupResponse response = stub.lookup(
                LookupRequest.newBuilder()
                        .setServiceName(SERVICE_NAME)
                        .setQualifier(qualifier)
                        .build()
        );

        // get list of servers
        return response.getServerList().get(0).getAddress();
    }

    public List<ManagedChannel> getServersChannels() throws StatusRuntimeException {
        List<ManagedChannel> channels = new ArrayList<ManagedChannel>();
        for (String qualifier : QUALIFIERS) {
            channels.add(this.getChannel(qualifier));
        }
        return channels;
    }

    public ManagedChannel getChannel(String qualifier) throws StatusRuntimeException {

        // To connect to servera
        ServerAddress address = this.lookup(qualifier);
        String host = address.getHost();
        int port = address.getPort();

        Logger.debug("Establishing connection to server " + qualifier + " at " + host + ":" + port);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        return channel;
    }

    @Override
    public void close() {
        channel.shutdown();
    }

}
