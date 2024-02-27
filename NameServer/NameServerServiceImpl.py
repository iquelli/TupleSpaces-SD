import sys

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")

import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
import logging
import grpc
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
        if server_entry in self.servers:
            raise UnsuccessfulServerRegisterException

        self.servers.append(server_entry)

    def search_for_servers(self, qualifier):
        servers_list = []
        if qualifier == "":
            return self.servers

        if not validate_qualifier(qualifier):
            raise InvalidServerArgumentsException

        servers_list.append(
            server for server in self.servers if server.qualifier == qualifier
        )

        return servers_list

    def remove_server(self, host, port):
        try:
            self.servers.remove(
                next(
                    server
                    for server in self.servers
                    if server.host == host and server.port == port
                )
            )
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
            logging.info("Receiving register request:\n" + str(request))
            service_name = request.serviceName
            qualifier = request.qualifier
            host = request.address.host
            port = request.address.port

            self.server.service_map[service_name].add_server(
                ServerEntry(host, port, qualifier)
            )

            return pb2.RegisterResponse()
        except InvalidServerArgumentsException:
            logging.debug("Server has invalid arguments")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Server has invalid arguments")
            return pb2.RegisterResponse()
        except UnsuccessfulServerRegisterException:
            logging.debug("Not possible to register the server")
            context.set_code(grpc.StatusCode.ALREADY_EXISTS)
            context.set_details("Not possible to register the server")
            return pb2.RegisterResponse()

    def lookup(self, request, context):
        try:
            logging.info("Receiving lookup request:\n" + str(request))
            service_name = request.serviceName
            qualifier = request.qualifier

            servers = self.server.service_map[service_name].search_for_servers(
                qualifier
            )

            response = pb2.LookupResponse()
            for s in servers:
                server_info = response.server.add()
                server_info.address.host = s.host
                server_info.address.port = s.port
                server_info.qualifier = s.qualifier

            if len(servers) == 0:
                logging.debug("Cannot resolve server with qualifier '%s'", qualifier)
                context.set_code(grpc.StatusCode.NOT_FOUND)
                context.set_details(
                    f"Cannot resolve server with qualifier '{qualifier}'"
                )
            return response
        except InvalidServerArgumentsException:
            logging.debug("Server has invalid arguments")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Server has invalid arguments")
            return pb2.LookupResponse()

    def delete(self, request, context):
        try:
            logging.info("Receiving delete request:\n" + str(request))
            service_name = request.serviceName
            host = request.address.host
            port = request.address.port

            self.server.service_map[service_name].remove_server(host, port)

            return pb2.DeleteResponse()
        except UnsuccessfulServerDeleteException:
            logging.debug("Not possible to remove the server")
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Not possible to remove the server")
            return pb2.DeleteResponse()
