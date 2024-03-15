from utils import *


# This class contains information for each server, namely its address and qualifier
class ServerEntry:
    def __init__(self, host, port, qualifier):
        if not (
            validate_host(host)
            and validate_port(port)
            and validate_qualifier(qualifier)
        ):
            raise InvalidServerArgumentsException

        self.host = host
        self.port = port
        self.qualifier = qualifier


# This class will save a service name and a set of server entries
class ServiceEntry:
    def __init__(self, service_name):
        self.service_name = service_name
        self.servers = []

    def add_server(self, server_entry):
        if any(server_entry.qualifier == s.qualifier for s in self.servers) or any(
            server_entry.port == s.port for s in self.servers
        ):
            raise UnsuccessfulServerRegisterException
        self.servers.append(server_entry)

    def get_servers(self):
        return self.servers

    def remove_server(self, host, port):
        for server in self.servers:
            if server.host == host and server.port == port:
                self.servers.remove(server)
                return
        raise UnsuccessfulServerDeleteException


# This class is responsible for mapping a service name to its
# corresponding ServiceEntry
class NameServer:
    def __init__(self):
        self.service_map = {}

    def register_service(self, service_name):
        if service_name not in self.service_map:
            self.service_map[service_name] = ServiceEntry(service_name)
