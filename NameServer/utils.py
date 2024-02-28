"""
Utils file for exceptions and argument validation.
"""

"""
EXCEPTIONS
"""


class UnsuccessfulServerRegisterException(Exception):
    """Exception raised when it's not possible to register the server."""

    def __init__(self):
        self.message = "Not possible to register the server"
        super().__init__(self.message)


class UnsuccessfulServerDeleteException(Exception):
    """Exception raised when it's not possible to delete the server."""

    def __init__(self):
        self.message = "Not possible to remove the server"
        super().__init__(self.message)


class InvalidServerArgumentsException(Exception):
    """Exception raised when the server has invalid arguments."""

    def __init__(self):
        self.message = "Server has invalid arguments"
        super().__init__(self.message)


"""
FUNCTIONS
"""


def validate_host(host):
    return host != ""


def validate_port(port):
    try:
        port_num = int(port)
        if not (1024 <= port_num <= 65535):
            return False
    except ValueError:
        return False

    return True


def validate_qualifier(qualifier):
    return qualifier is not None
