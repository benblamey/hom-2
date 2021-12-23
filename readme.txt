# HASTE-o-MATIC
#############

Tiered, streamed, data management tool.
See the demo:
https://www.dropbox.com/s/lz5l35g7q9l6lli/haste-o-matic-demo-dec.mov?dl=0

Part of the HASTE Project. http://haste.research.it.uu.se/


# DEPLOYMENT INSTRUCTIONS FOR UBUNTU 21.04
#############

# Connect to the server forwarding ports to these ports on the server:
#  localhost:10443 (for the microK8s dash)
#  localhost:80 (for the HTTP services)

sudo apt update

sudo snap install microk8s --classic

sudo microk8s enable dns
sudo microk8s enable ingress


sudo apt -y install git
git clone https://github.com/benblamey/hom-2.git

# modify the persistent volume to match the current machine (check the host and path)
# The lines are near the top:
head -n 50 hom-2/kubernetes/k8.yaml

sudo microk8s kubectl create namespace hom
sudo microk8s kubectl config set-context --current --namespace=hom

sudo microk8s kubectl apply -f hom-2/kubernetes/k8.yaml

# See if everything is running:
sudo microk8s kubectl get all --all-namespaces

# Access the Kubernetes dashboard (this keeps running so recommended open in a new session)
sudo microk8s dashboard-proxy

# port forward the web ingress to localhost (in the background)
sudo microk8s kubectl port-forward --namespace=ingress daemonset.apps/nginx-ingress-microk8s-controller 80:80 &

# If port forwarding is setup correctly, you can now access:
# http://localhost/gui/ (the GUI)
# http://localhost/notebook/ (Jupyer) The password is hej-hom-impl-foo

# (re)Start the demo application can restart/begin streaming data:
sudo microk8s kubectl delete pod demo-data ; sudo microk8s kubectl run demo-data --image benblamey/hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Always' --restart=Always -- java -cp output.jar -Droot.log.level=DEBUG -Dcom.benblamey.hom.demodata.DemoDataProducer.log.level=DEBUG -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.demodata.DemoDataMain

Go into the GUI and add an input tier for "haste-input-data".

Go into Jupyter and run tier-0 notebook to analyze the sample tier, following the video tutorial.