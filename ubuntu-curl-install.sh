# Connect to the server forwarding ports to these ports on the server:
#  localhost:10443 (for the microK8s dash)
#  localhost:80 (for the HTTP services)
# See: https://www.ibm.com/support/pages/what-are-ssh-tunnels-and-how-use-them


sudo apt update ; sudo apt upgrade -y ; sudo snap install microk8s --classic --channel=1.21/stable ; sudo microk8s enable dns ingress rbac

# Access the Kubernetes admin dashboard (this keeps running so recommended open in a new session, or use &). Binds to https://127.0.0.1:10443
sudo microk8s dashboard-proxy &

sudo apt -y install git ; git clone https://github.com/HASTE-project/hom-2.git

sudo microk8s kubectl create namespace hom ; sudo microk8s kubectl config set-context --current --namespace=hom

# modify the persistent volume to match the current machine (check the host and path)

# or attempt this with sed..
sed -i s/hom-2-benblamey/$(hostname)/ hom-2/kubernetes/storage.yaml
sed -i s+/home/ubuntu/mnt+$(pwd)+ hom-2/kubernetes/storage.yaml
sudo microk8s kubectl apply -f hom-2/kubernetes/storage.yaml

sudo microk8s kubectl apply -f hom-2/kubernetes/k8.yaml

# See if everything is running:
sudo microk8s kubectl get all --all-namespaces

# port forward the web ingress to localhost (in the background)
sudo microk8s kubectl port-forward --namespace=ingress daemonset.apps/nginx-ingress-microk8s-controller 80:80 &

echo -- HOM is Ready! --