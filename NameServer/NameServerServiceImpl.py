import sys

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from utils import *


# this class contains information for each server, namely its address and qualifier
class ServerEntry:
    def __init__(self, qualifier, host, port):
        if not (
            validate_host(host)
            and validate_qualifier(qualifier)
            and validate_port(port)
        ):
            raise InvalidServerArguments

        self.qualifier = qualifier
        self.host = host
        self.port = port

    def __str__(self):
        return f"ServerEntry(host={self.host}, port={self.port} qualifier={self.qualifier})"


# this class will save a service name and a set of server entries
class ServiceEntry:
    def __init__(self, service_name):
        self.service_name = service_name
        self.servers = []

    def __str__(self):
        servers_str = ", ".join(str(server) for server in self.servers)
        return (
            f"ServiceEntry(service_name={self.service_name}, servers=[{servers_str}])"
        )

    def add_server(self, server_entry):
        if server_entry in self.servers:
            raise UnsuccessfulServerRegister

        self.servers.append(server_entry)

    def search_for_servers(self, qualifier):
        servers_list = []
        if qualifier is None:
            return self.servers

        if not validate_qualifier(qualifier):
            raise InvalidServerArguments

        for server in self.servers:
            if server.qualifier == qualifier:
                servers_list.append(server)
        return servers_list

    def remove_server(self, server_entry):
        if server_entry not in self.servers:
            raise UnsuccessfulServerDelete

        self.servers.remove(server_entry)


# this class it's responsible for mapping a service name to its corresponding ServiceEntry
class NamingServer:
    def __init__(self):
        self.service_map = {}
        self.register_service("TupleSpaces")

    def register_service(self, service_name):
        if service_name not in self.service_map:
            self.service_map[service_name] = ServiceEntry(service_name)


class NameServerServiceImpl(pb2_grpc.NameServerServicer):
    def __init__(self, *args, **kwargs):
        self.server = NamingServer()

    def register(self, request, context):
        # print the received request
        try:
            print(request)
            # get service name
            service_name = request.serviceName
            # get server qualifier
            qualifier = request.qualifier
            # get server address
            host = request.address.host
            port = request.address.port

            self.server.service_map[service_name].add_server(
                ServerEntry(qualifier, host, port)
            )

            # create response
            response = pb2.RegisterResponse()

            # return response
            return response

        except (UnsuccessfulServerRegister, InvalidServerArguments) as e:
            print("Registration failed:", e.message)

    def lookup(self, request, context):
        try:
            # print the received request
            print(request)
            # get service name
            service_name = request.serviceName
            # get server qualifier
            qualifier = request.qualifier

            servers = self.server.service_map[service_name].search_for_servers(
                qualifier
            )

            # create response
            response = pb2.LookupResponse()
            for server in servers:
                server_info = response.server.add()
                server_info.address.host = server.host
                server_info.address.port = server.port
                server_info.qualifier = server.qualifier

            # return response
            return response

        except InvalidServerArguments as e:
            print("Lookup failed:", e.message)

    def delete(self, request, context):
        try:
            # print the received request
            print(request)

            # get service name
            service_name = request.service_name
            # get server qualifier
            qualifier = request.qualifier
            # get server address
            address = request.address

            self.server.service_map[service_name].remove(
                ServerEntry(qualifier, address)
            )

            # create response
            response = pb2.DeleteResponse()

            # return response
            return response

        except (UnsuccessfulServerDelete, InvalidServerArguments) as e:
            print("Delete failed:", e.message)
