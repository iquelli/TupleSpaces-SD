package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.grpc.NameServerService;

public class ServerManager {

    private final int port;
    private final String qualifier;
    private final NameServerService nameServerService = new NameServerService();

    public ServerManager(int port, String qualifier) {
        this.port = port;
        this.qualifier = qualifier;
    }

    public void registerToNameServer() {
        nameServerService.register(port, qualifier);
    }

    public void deleteFromNameServer() {
        nameServerService.delete(port);
    }

    public void shutdown() {
        nameServerService.close();
    }

}
