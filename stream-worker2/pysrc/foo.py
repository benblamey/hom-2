import sys


class SimpleHello(object):

    def sayHello(self, int_value=None, string_value=None):
        print(int_value, string_value)
        sys.stdout.flush()
        return "Said hello to {0}".format(string_value)

    class Java:
        implements = ["com.benblamey.hom.py.IHello"]

# Make sure that the python code is started first.
# Then execute: java -cp py4j.jar py4j.examples.SingleThreadClientApplication
from py4j.java_gateway import JavaGateway, CallbackServerParameters

import logging
logger = logging.getLogger("py4j")
logger.setLevel(logging.DEBUG)
logger.addHandler(logging.StreamHandler())

simple_hello = SimpleHello()
gateway = JavaGateway(
    callback_server_parameters=CallbackServerParameters(),
    python_server_entry_point=simple_hello)

print("hello from python")
