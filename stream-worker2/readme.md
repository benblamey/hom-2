kubectl config use-context docker-desktop

kubectl delete pod kafka ; kubectl apply -f kubernetes/k8.yaml

port forward, Run this each time the kafka is started:
kubectl port-forward --address localhost pods/kafka 19092:19092

Stream demo data (and attach):
kubectl delete pod demo-data ; kubectl run demo-data --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.demodata.DemoDataMain 

Stream process data with JEXL (and attach):
kubectl delete pod engine-1 ; kubectl run engine-1 --image hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.engine.PipelineEngineMain 




---
# Development
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
