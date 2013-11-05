#! /bin/bash

echo "cloudera-manager-stop.sh: About to stop cloudera manager services..."

## starting cloudera-manager-server
echo "cloudera-manager-stop.sh: stopping cloudera-manager-server..."
sudo service cloudera-scm-server stop

## stopping cloudera-manager-server-db
echo "cloudera-manager-stop.sh: stopping cloudera-manager-server-db..."
sudo service cloudera-scm-server-db stop
