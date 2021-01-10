#!/bin/sh

echo "1. Build JAR"
echo
mvn clean install
echo

echo "2. Stop service"
ssh -i ~/.ssh/id_rsa benjavalero@login.toolforge.org "become replacer webservice stop"

# Check status
service_status=$(ssh -i ~/.ssh/id_rsa benjavalero@login.toolforge.org "become replacer webservice status")
echo $service_status
until [ "$service_status" == "Your webservice is not running" ]; do
  echo "Wait 5 seconds and retry"
  sleep 5 # 5 seconds
  service_status=$(ssh -i ~/.ssh/id_rsa benjavalero@login.toolforge.org "become replacer webservice status")
  echo $service_status
done
echo

echo "3. Copy JAR"
scp -i ~/.ssh/id_rsa replacer-backend/target/replacer.jar benjavalero@login.toolforge.org:/data/project/replacer
echo

echo "5. Start service"
ssh -i ~/.ssh/id_rsa benjavalero@login.toolforge.org "become replacer webservice start"
echo

echo "Done"