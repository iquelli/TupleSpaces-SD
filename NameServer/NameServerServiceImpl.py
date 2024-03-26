import sys
import logging
import grpc
from domain import *

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")

import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc


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

            if service_name not in self.server.service_map:
                self.server.register_service(service_name)
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
            if not validate_qualifier(qualifier):
                raise InvalidServerArgumentsException

            servers = []
            if service_name in self.server.service_map:
                servers = self.server.service_map[service_name].get_servers()

            response = pb2.LookupResponse()
            added_server = False
            for s in servers:
                if s.qualifier == qualifier or qualifier == "":
                    added_server = True
                    server_info = response.server.add()
                    server_info.address.host = s.host
                    server_info.address.port = s.port
                    server_info.qualifier = s.qualifier

            if not added_server:
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

            if service_name in self.server.service_map:
                self.server.service_map[service_name].remove_server(host, port)

            return pb2.DeleteResponse()
        except UnsuccessfulServerDeleteException:
            logging.debug("Not possible to remove the server")
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Not possible to remove the server")
            return pb2.DeleteResponse()
