

from setuptools import setup, find_packages

setup(
    name="py_stream_worker",
    version="0.1",
    packages=find_packages(),
    install_requires=[
        "kafka-python==2.0.2",
    ]
)