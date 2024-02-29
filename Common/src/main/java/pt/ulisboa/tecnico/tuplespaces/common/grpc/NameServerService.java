package pt.ulisboa.tecnico.tuplespaces.common.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.DeleteRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.LookupResponse;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.PingRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.PingResponse;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.RegisterRequest;
import pt.ulisboa.tecnico.tuplespaces.nameserver.contract.NameServerOuterClass.ServerAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NameServerService implements AutoCloseable {

    private static final String NAME_SERVER_HOST = "localhost";
    private static final int NAME_SERVER_PORT = 5001;
    private static final String SERVICE_NAME = "TupleSpaces";

    private final ManagedChannel channel;
    private final NameServerGrpc.NameServerBlockingStub stub;

    // Map with all the connections estabilished
    private final Map<String, ChannelStubPair<TupleSpacesBlockingStub>> channelStubPairMap =
            new ConcurrentHashMap<>();

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

    public Boolean ping(String qualifier) {
        PingResponse response = stub.ping(
                PingRequest.newBuilder()
                        .setServiceName(SERVICE_NAME)
                        .setQualifier(qualifier)
                        .build()
        );
        return response.getAnswer();
    }

    public TupleSpacesBlockingStub connectToServer(String qualifier) throws StatusRuntimeException {
        ChannelStubPair<TupleSpacesBlockingStub> channelAndStub = this.channelStubPairMap.get(
                qualifier
        );

        if (channelAndStub != null && !channelAndStub.channel().isTerminated()) {
            if (this.ping(qualifier)) {
                return channelAndStub.stub(); // channel was already created, no need to create again
            }
            channelAndStub.channel().shutdown();
            this.channelStubPairMap.remove(qualifier);
        }

        // To connect to server
        ServerAddress address = this.lookup(qualifier);
        String host = address.getHost();
        int port = address.getPort();

        Logger.debug("Establishing connection to server " + qualifier + " at " + host + ":" + port);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        TupleSpacesBlockingStub stub = TupleSpacesGrpc.newBlockingStub(channel);

        Logger.debug("Connected to server " + qualifier + " at " + host + ":" + port);

        this.channelStubPairMap.put(qualifier, new ChannelStubPair<>(channel, stub));
        return stub;
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

    @Override
    public void close() {
        channel.shutdown();
        this.channelStubPairMap.forEach((k, v) -> v.channel().shutdown());
        this.channelStubPairMap.clear();
    }

    public record ChannelStubPair<T>(ManagedChannel channel, T stub) {
    }

}
