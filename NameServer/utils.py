"""
Utils file.
"""

"""
EXCEPTIONS
"""

class UnsuccessfulServerRegister(Exception):
    """Exception raised when it's not possible to register the server."""

    def __init__(self, message):
        super().__init__(message)
        self.message = "Not possible to register the server"


class UnsuccessfulServerDelete(Exception):
    """Exception raised when it's not possible to delete the server."""

    def __init__(self, message):
        super().__init__(message)
        self.message = "Not possible to remove the server"


class InvalidServerArguments(Exception):
    """Exception raised when it's not possible to delete the server."""

    def __init__(self, message):
        super().__init__(message)
        self.message = "Server has invalid arguments"


"""
FUNCTIONS
"""


# To validate received server address
def validate_address(address):
    # Split the address into host and port
    host, port = address.split(':')

    # Validate host
    if not host:
        return False

    # Validate port number
    try:
        port_num = int(port)
        if not (1024 <= port_num <= 65535):
            return False
    except ValueError:
        return False

    return True


# To validate received server qualifier
def validate_qualifier(qualifier):
    # Split the address into host and port

    # Validate host
    if qualifier not in ["A", "B", "C"]:
        return False

    return True
