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


def validate_host(host):
    if not host:
        return False
    return True



def validate_port(port):
    try:
        port_num = int(port)
        if not (1024 <= port_num <= 65535):
            return False
    except ValueError:
        return False
    return True
    


def validate_qualifier(qualifier):
    if qualifier not in ["A", "B", "C"]:
        return False

    return True

