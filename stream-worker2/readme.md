kubectl config use-context docker-desktop

kubectl delete pod kafka ; kubectl apply -f kubernetes/haste-k8.yaml

Set up kafka topics.

Get shell inside kafka pod:
kubectl exec --stdin --tty kafka -- /bin/bash

bin/kafka-topics.sh --create \
--bootstrap-server kafka-service:9092 \
--replication-factor 1 \
--partitions 1 \
--topic haste-input-data


bin/kafka-topics.sh --list --bootstrap-server kafka-service:9092

Run this each time the pod is started:
kubectl port-forward --address localhost pods/kafka 19092:19092

kubectl delete pod demo-data ; kubectl run demo-data --image haste-o-matic-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Never' --restart=Always -- java -cp output.jar -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.haste.demodata.HasteDemoDataMain 