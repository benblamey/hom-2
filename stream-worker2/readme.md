kubectl config use-context docker-desktop





kubectl delete persistentvolumeclaim/hom-pv-claim persistentvolume/hom-pv deployment.apps/py-stream-worker-deployment pods/notebook ; kubectl apply -f kubernetes/k8.yaml

kubectl delete deployment py-stream-worker-deployment ; kubectl apply -f kubernetes/k8.yaml
kubectl delete pod manager ; kubectl apply -f kubernetes/k8.yaml
kubectl delete pod notebook ; kubectl apply -f kubernetes/k8.yaml
kubectl port-forward --address localhost pods/manager 4567:4567
kubectl port-forward --address localhost pods/notebook 8888:8888

Stream demo data (and attach):
kubectl delete pod demo-data ; kubectl run demo-data --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.demodata.DemoDataMain 


kubectl delete pod/demo-data ; kubectl run demo-data --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- bash -c "while true; do echo -n .; sleep 1; done"

Stream process data with JEXL (and attach):
kubectl delete pod engine-1 ; kubectl run engine-1 --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 -DKAFKA_APPLICATION_ID=app-hom-tier-3 -DINPUT_TOPIC=haste-input-data -DOUTPUT_TOPIC=hom-tier-3 -DJEXL_EXPRESSION="data.foo > 42" com.benblamey.hom.engine.PipelineEngineMain 

Run and attach manager pod ('test walkthrough'):
kubectl delete pod manager ; kubectl run manager --image hom-impl-2.manager --image-pull-policy='Never' --restart=Always --command --attach='true' --stdin -- java -agentlib:"jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.manager.ManagerMainREST

kubectl port-forward --address localhost pods/manager 5005:5005

Port forwarding for REST API on manager:
kubectl port-forward --address localhost pods/manager 4567:4567

TODO:
Notebook password is:
hej-hom-impl-foo
,"--NotebookApp.password", "argon2:$argon2id$v=19$m=10240,t=10,p=8$LKoe+YVbK+qMsaH87iMiLQ$XroRerX0xeyPoBB1Ik2odA",
(To generate new password hash: from notebook.auth import passwd; passwd() )

---
# Development
port forward, Run this each time the kafka is started for local debugging:
kubectl port-forward --address localhost pods/kafka 19092:19092

Get shell inside kafka pod:
kubectl exec --stdin --tty kafka -- /bin/bash
kubectl exec --stdin --tty notebook -- /bin/bash

need --privileged=true to do mounting. see https://stackoverflow.com/questions/36553617/how-do-i-mount-bind-inside-a-docker-container
docker run -i --tty --privileged=true c014e6306fdd /bin/bash

docker run -i --tty notebook


Set up kafka topics.
(can skip this, topic auto-creation is now enabled )
bin/kafka-topics.sh --create \
--bootstrap-server kafka-service:9092 \
--replication-factor 1 \
--partitions 1 \
--topic haste-input-data

bin/kafka-topics.sh --list --bootstrap-server kafka-service:9092 
bin/kafka-topics.sh --describe hom-topic-0-d2d5aebf-d61b-4f07-9c1c-b1fe6aff54a4 --bootstrap-server kafka-service:9092
bin/kafka-topics.sh --describe haste-input-data --bootstrap-server kafka-service:9092
./bin/kafka-consumer-groups.sh --bootstrap-server kafka-service:9092 --describe --all-groups


Jupyter Notebook CLI:
usage: __main__.py [-h] [--debug] [--show-config] [--show-config-json] [--generate-config] [-y] [--allow-root] [--no-browser] [--autoreload] [--script] [--no-script] [--core-mode] [--dev-mode]
[--splice-source] [--expose-app-in-browser] [--extensions-in-dev-mode] [--collaborative] [--log-level ServerApp.log_level] [--config ServerApp.config_file] [--ip ServerApp.ip]
[--port ServerApp.port] [--port-retries ServerApp.port_retries] [--sock ServerApp.sock] [--sock-mode ServerApp.sock_mode] [--transport KernelManager.transport]
[--keyfile ServerApp.keyfile] [--certfile ServerApp.certfile] [--client-ca ServerApp.client_ca] [--notebook-dir ServerApp.root_dir] [--preferred-dir ServerApp.preferred_dir]
[--browser ServerApp.browser] [--pylab ServerApp.pylab] [--gateway-url GatewayClient.url] [--watch [LabApp.watch]] [--app-dir LabApp.app_dir]
[extra_args ...]

TODO:
first input tier is a bit weird
sampling.
    have the manager, on tier creation, create a listener which streams 500 elements to a file.
    also, create a python notebook from a template, with the filename pre-loaded. 
