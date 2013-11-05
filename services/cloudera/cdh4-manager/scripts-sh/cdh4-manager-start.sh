#! /bin/bash

echo "cloudera-manager-start.sh: About to start cloudera manager services..."
## starting cloudera-manager-server-db
echo "cloudera-manager-start.sh: starting cloudera-manager-server-db..."
sudo service cloudera-scm-server-db start

## starting cloudera-manager-server
echo "cloudera-manager-start.sh: starting cloudera-manager-server..."
sudo service cloudera-scm-server start



