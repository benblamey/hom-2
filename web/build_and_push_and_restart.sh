kubectl config set-context --current --namespace=hom
kubectl delete pod/static-web
docker build -t benblamey/hom-impl-2.web:latest .
docker push benblamey/hom-impl-2.web:latest
kubectl apply -f ../kubernetes/k8.yaml