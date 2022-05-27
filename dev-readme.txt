## Admin HowTos

### Manually admin the underlying kafka topics:

bin/kafka-topics.sh --bootstrap-server kafka-service:9092 --list
bin/kafka-topics.sh --bootstrap-server kafka-service:9092 --delete --topic wiki_tier_0
bin/kafka-consumer-groups.sh --bootstrap-server kafka-service:9092 --describe --all-groups
bin/kafka-consumer-groups.sh --bootstrap-server kafka-service:9092 --delete --group sampler
bin/kafka-topics.sh --bootstrap-server kafka-service:9092 --list | grep hom-topic | xargs -L1 bin/kafka-topics.sh --bootstrap-server kafka-service:9092 --delete --topic

sudo microk8s kubectl describe pods
sudo microk8s kubectl delete -n hom pod kafka
sudo microk8s kubectl delete --all deployment -n hom
sudo microk8s kubectl delete --all pod -n hom

### Local Dev Help Commands

kubectl config get-contexts
kubectl config use-context docker-desktop

For Docker Desktop, we are supposed to use the nginx-ingress controller as described in:loc
https://kubernetes.github.io/ingress-nginx/deploy/#quick-start
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.2.0/deploy/static/provider/cloud/deploy.yaml

...don't Modify the 'ingress' resource class name in the YAML.
sudo kubectl port-forward --namespace=ingress-nginx service/ingress-nginx-controller 80:80

kubectl create namespace hom
kubectl config set-context --current --namespace=hom
kubectl apply -f kubernetes/k8.yaml



