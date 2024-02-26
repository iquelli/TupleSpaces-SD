import sys

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")

import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc

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

    def __str__(self):
        return f"ServerEntry(host={self.host}, port={self.port}, qualifier={self.qualifier})"


# This class will save a service name and a set of server entries
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
            raise UnsuccessfulServerRegisterException

        self.servers.append(server_entry)

    def search_for_servers(self, qualifier):
        servers_list = []
        if qualifier is None:
            return self.servers

        if not validate_qualifier(qualifier):
            raise InvalidServerArgumentsException

        servers_list.append(server for server in self.servers if server.qualifier == qualifier)

        return servers_list

    def remove_server(self, host, port):
        try:
            self.servers.remove(next(server for server in self.servers if server.host == host and server.port == port))
        except StopIteration:
            raise UnsuccessfulServerDeleteException


# This class is responsible for mapping a service name to its
# corresponding ServiceEntry
class NameServer:
    def __init__(self):
        self.service_map = {}
        self.register_service("TupleSpaces")

    def register_service(self, service_name):
        if service_name not in self.service_map:
            self.service_map[service_name] = ServiceEntry(service_name)


# This class implements the register, lookup and delete operations
class NameServerServiceImpl(pb2_grpc.NameServerServicer):
    def __init__(self, *args, **kwargs):
        self.server = NameServer()

    def register(self, request, context):
        try:
            print("Receiving register request:")
            print(request)
            service_name = request.serviceName
            qualifier = request.qualifier
            host = request.address.host
            port = request.address.port

            self.server.service_map[service_name].add_server(
                ServerEntry(host, port, qualifier)
            )

            response = pb2.RegisterResponse()
            return response

        except (
            UnsuccessfulServerRegisterException,
            InvalidServerArgumentsException,
        ) as e:
            print("Registration failed: ", e.message)

    def lookup(self, request, context):
        try:
            print("Receiving lookup request:")
            print(request)
            service_name = request.serviceName
            qualifier = request.qualifier

            servers = self.server.service_map[service_name].search_for_servers(
                qualifier
            )

            response = pb2.LookupResponse()
            for server in servers:
                server_info = response.server.add()
                server_info.address.host = server.host
                server_info.address.port = server.port
                server_info.qualifier = server.qualifier
            return response

        except InvalidServerArgumentsException as e:
            print("Lookup failed: ", e.message)

    def delete(self, request, context):
        try:
            print("Receiving delete request:")
            print(request)
            service_name = request.serviceName
            host = request.address.host
            port = request.address.port

            self.server.service_map[service_name].remove_server(
                host, port
            )

            response = pb2.DeleteResponse()
            return response

        except (
            UnsuccessfulServerDeleteException,
            InvalidServerArgumentsException,
        ) as e:
            print("Delete failed: ", e.message)
