import sys
import logging
import argparse
import grpc
from NameServerServiceImpl import NameServerServiceImpl
from concurrent import futures

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")

import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc

# Define the port
PORT = 5001

if __name__ == "__main__":
    try:
        if len(sys.argv) > 2:
            print("Too many arguments passed!")
            print("Usage: python ./server.py [-log=<log_level>]")
            exit(1)

        # Setup logger
        parser = argparse.ArgumentParser()
        parser.add_argument("-log", default="WARNING")
        logging.basicConfig(
            format="[%(levelname)s]: %(message)s",
            level=parser.parse_args().log.upper(),
        )

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
        print("Press CTRL+C to terminate\n")
        # Wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("NameServer stopped")
        exit(0)
