#! /bin/bash

echo "cdh4-node-install.sh: About to install cdh4 packages, mongoDB..."

##updating the sources list first
sudo apt-get update

## installing packages
echo "cdh4-node-install.sh: Installing packages: bigtop-utils bigtop-jsvc bigtop-tomcat hadoop hadoop-hdfs hadoop-httpfs hadoop-mapreduce hadoop-yarn hadoop-client hadoop-0.20-mapreduce hue-plugins hbase hive oozie oozie-client pig zookeeper..."
sudo apt-get -q --force-yes -y install bigtop-utils bigtop-jsvc bigtop-tomcat hadoop hadoop-hdfs hadoop-httpfs hadoop-mapreduce hadoop-yarn hadoop-client hadoop-0.20-mapreduce 
sudo apt-get -q --force-yes -y install hue-plugins hbase hive oozie oozie-client pig zookeeper hue flume-ng mahout sqoop2 sqoop2-server
echo "cdh4-node-install.sh: cdh4 packages are installed!..."

echo "cdh4-node-install.sh: Installing packages: mongodb"
sudo apt-get -q --force-yes -y install mongodb-10gen=2.4.4

sudo apt-get -q --force-yes -y install libmysql-java

echo "cdh4-node-install.sh: About to install cloudera agent"

sudo apt-get -q --force-yes -y install cloudera-manager-agent cloudera-manager-daemons

echo "cdh4-node-install.sh: cloudera agents installed!..."