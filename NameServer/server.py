import sys
sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NameServerServiceImpl import NameServerServiceImpl
from concurrent import futures


# define the port
PORT = 5001


if __name__ == "__main__":
    try:
        if len(sys.argv) != 1:
            print("Server does not receive arguments!")
            exit(1)

        print("NameServer started")
        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        # add service
        pb2_grpc.add_NameServerServicer_to_server(NameServerServiceImpl(), server)
        # listen on port
        server.add_insecure_port('[::]:'+str(PORT))
        # start server
        server.start()
        # print message
        print("Server listening on port " + str(PORT))
        # print termination message
        print("Press CTRL+C to terminate")
        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("NameServer stopped")
        exit(0)
