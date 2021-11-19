kubectl config use-context docker-desktop

kubectl delete pod kafka ; kubectl apply -f kubernetes/k8.yaml

Stream demo data (and attach):
kubectl delete pod demo-data ; kubectl run demo-data --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.demodata.DemoDataMain 

Stream process data with JEXL (and attach):
kubectl delete pod engine-1 ; kubectl run engine-1 --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 -DKAFKA_APPLICATION_ID=app-hom-tier-3 -DINPUT_TOPIC=haste-input-data -DOUTPUT_TOPIC=hom-tier-3 -DJEXL_EXPRESSION="data.foo > 42" com.benblamey.hom.engine.PipelineEngineMain 

Run and attach manager pod ('test walkthrough'):
kubectl delete pod manager ; kubectl run manager-test --image hom-impl-2.manager --image-pull-policy='Never' --restart=Always --command --attach='true' --stdin -- java -agentlib:"jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.manager.ManagerMainTest

kubectl port-forward --address localhost pods/manager 5005:5005

Port forwarding for REST API on manager:
kubectl port-forward --address localhost pods/manager 4567:4567

---
# Development
port forward, Run this each time the kafka is started for local debugging:
kubectl port-forward --address localhost pods/kafka 19092:19092


Get shell inside kafka pod:
kubectl exec --stdin --tty kafka -- /bin/bash


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
