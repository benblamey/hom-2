import sys
import logging
from kafka import KafkaConsumer, KafkaProducer
import json
import importlib.util

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

# python3 -m py_stream_worker kafka-service:9092 haste-input-data output-topic-foo groupidfoo example.py hej

#  TODO: kafka.coordinator.consumer - WARNING - group_id is None: disabling auto-commit.

kakfa_bootstrap_server = sys.argv[1]
input_topic = sys.argv[2]
output_topic = sys.argv[3]
group_id = sys.argv[4]
python_filepath = sys.argv[5]  # some-guid.py
python_function = sys.argv[6]  # bar

logging.info("Command line arguments:")
logging.info({
    'kakfa_bootstrap_server': kakfa_bootstrap_server,
    'input_topic': input_topic,
    'output_topic': output_topic,
    'group_id': group_id,
    'python_filepath': python_filepath,
    'python_function': python_function,
})

read_a_message = False
consumer = KafkaConsumer(input_topic,
                         bootstrap_servers=kakfa_bootstrap_server,
                         group_id=group_id,
                         auto_offset_reset='earliest')
producer = KafkaProducer(bootstrap_servers=kakfa_bootstrap_server)

# TODO: have one thread listen for shutdown, and exit gracefully.

print('started.')

import_spec = importlib.util.spec_from_file_location("my_notebook", python_filepath)
imported_module = importlib.util.module_from_spec(import_spec)
loaded_module = import_spec.loader.exec_module(imported_module)
users_function = getattr(imported_module, python_function)

# result = users_function({"foo": 1})
# print(result)
# # {'accept': True}
# accept = result['accept']
# del result['accept']
# print(result)


for msg in consumer:
    if not read_a_message:
        logging.info("successfully read a message from Kafka.")
        read_a_message = True

    input_dict = json.loads(msg.value)

    output_dict = users_function(input_dict)

    logging.debug(f'output dictionary is: {output_dict}')

    if not output_dict:
        logging.debug('Returned object is falsy so will not be written to next tier.')
        continue

    if 'accept' in output_dict:
        logging.debug(" 'accept' key detected. Use of this field is deprecated. It is preferred simply to return a falsy value, like None, to exclude an object.")
        accept = output_dict.pop("accept", None)
        if not accept:
            logging.debug("skipping object because 'accept' was truthy.")
            continue

    key = msg.key  # needs to be bytes
    value = json.dumps(output_dict).encode('utf-8')  # needs to be bytes
    producer.send(output_topic, value=value, key=key)


# import nbformat
# from nbconvert.preprocessors import ExecutePreprocessor

# >>> # Serialize json messages
# >>> import json
# >>> producer = KafkaProducer(value_serializer=lambda v: json.dumps(v).encode('utf-8'))
# >>> producer.send('fizzbuzz', {'foo': 'bar'})


# ConsumerRecord(topic='haste-input-data', partition=0, offset=730426, timestamp=1638525995005, timestamp_type=0, key=b'\x00\x00\x00\x00\x00\x00\x11\xe5', value=b'{"bar":474.8429258795065,"foo":66,"name":"obj_4581","id":4581,"wibble":539.0576958760705}', headers=[], checksum=None, serialized_key_size=8, serialized_value_size=89, serialized_header_size=-1)


# producer = KafkaProducer(bootstrap_servers=kakfa_bootstrap_server)
#      ...     producer.send('foobar', b'some_message_bytes')


# logging.debug("opening notebookfile...")
# with open("/data/" + python_filename) as f:
#     logging.debug("calling nbformat.read...")
#     nb = nbformat.read(f, as_version=4)
#     logging.debug("calling ExecutePreprocessor...")
#     ep = ExecutePreprocessor(timeout=600, kernel_name='python3')
#     logging.debug("calling ep.preprocess...")
#     out = ep.preprocess(nb, {'metadata': {'path': '/data'}})
# print(out)
# print(hej({"foo": 2}))
