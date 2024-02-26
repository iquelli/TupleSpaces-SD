import sys

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")

import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NameServerServiceImpl import NameServerServiceImpl
from concurrent import futures

# Define the port
PORT = 5001

if __name__ == "__main__":
    try:
        if len(sys.argv) != 1:
            print("Server does not receive arguments!")
            exit(1)

        print("NameServer started")
        # Create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        # Add service
        pb2_grpc.add_NameServerServicer_to_server(NameServerServiceImpl(), server)
        # Listen on port
        server.add_insecure_port("[::]:" + str(PORT))
        # Start server
        server.start()
        # Print message
        print("Server listening on port " + str(PORT))
        # Print termination message
        print("Press CTRL+C to terminate")
        # Wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("NameServer stopped")
        exit(0)
