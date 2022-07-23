# HASTE-o-MATIC

Tiered, streamed, data management tool. 
See the demo: [https://www.dropbox.com/s/lz5l35g7q9l6lli/haste-o-matic-demo-dec.mov?dl=0]()

Part of the HASTE Project. [http://haste.research.it.uu.se/]()

## DEPLOYMENT INSTRUCTIONS FOR UBUNTU (21.04)

0. Connect to a fresh VM, forwarding ports to these ports on the server:
  localhost:8001 (for the microK8s dashboard)
  localhost:80 (for the HOM HTTP services)
See: [https://www.ibm.com/support/pages/what-are-ssh-tunnels-and-how-use-them]()

For example:
```
sudo ssh ubuntu@<host> -i <key-file> -L 80:localhost:80 -L 8001:localhost:8001
```

1. Run the install script via curl (or copy-paste the commands [from the script](ubuntu-curl-install.sh))
```
source <(curl -s https://raw.githubusercontent.com/HASTE-project/hom-2/main/ubuntu-curl-install.sh)
```

The token used to access the dashboard is printed in the console, it will look something like this:

```
--- BEGIN DASHBOARD TOKEN ---
eyJhbGciOiJSUzI1NiIsImtpZCI6IjFCdTBGNXlCV3V2djZyb0xvRWtUU193WnBxZGVZcjRaOWVhcFpJbkFsMk
EifQeyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZRlcy5pby9zZXJ2aWNlYWNjb3V
udC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXtH_CDT9TuTq3QntWw8_KbrbuUx6Qwteb8qL
1wj7do9kGz1HH-EKBPt39OpmyEFJtYLeLANF6s0xngJgU3Th8aly0DaLTh674brY5jciKqkiulSMFtK3SxUYux
aRPM-tgpFtHYAtgT8rk4q3dPbTRtJM5MPnsyaog96m6jTe8FX-e19Ad-OkVoJgdOWzN73iV9VI0WbrtYw
--- END DASHBOARD TOKEN ---
```

2. Assuming port forwarding is setup correctly, you can now access (don't forget the trailing slash):

[The Dashboard](http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/workloads?namespace=hom)

[http://localhost/gui/](http://localhost/gui/) (the GUI)

[http://localhost/notebook/](http://localhost/notebook/) (Jupyter) 
The password is `hej-hom-impl-foo` (Note that access to all the web services, including the notebook, is protected by the SSH login)

[http://localhost/kafka-gui/](http://localhost/kafka-gui/) (Kafka-ui) 

3. (re)Start the demo application can restart/begin streaming data:
```
sudo microk8s kubectl delete pod demo-data ; sudo microk8s kubectl run demo-data --image benblamey/hom-impl-2.stream-worker2 --attach='true' --stdin --command --image-pull-policy='Always' --restart=Always -- java -cp output.jar -Droot.log.level=DEBUG -Dcom.benblamey.hom.demodata.DemoDataProducer.log.level=DEBUG -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -DKAFKA_BOOTSTRAP_SERVER=kafka-service:9092 com.benblamey.hom.demodata.DemoDataMain
```

4. Go into [the GUI](http://localhost/gui/) and add an input tier for `haste-input-data`.

5. Go into Jupyter and run tier-0 notebook to analyze the sample tier, following the video tutorial.



Contributors: 
* Ben Blamey [http://www.benblamey.com](http://www.benblamey.com)
* Bipin Patel [https://github.com/BipinPatel](https://github.com/BipinPatel)
* Haoyuan Li [https://github.com/Haoyuan-L](https://github.com/Haoyuan-L)
