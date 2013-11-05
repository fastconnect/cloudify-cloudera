#! /bin/bash

function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

echo "cloudera-manager-install.sh: About to install cm4 packages..."

##updating the sources list first
sudo apt-get update || error_exit $? "Failed to update apt-get sources"

## installing cloudera-manager-server package
echo "cloudera-manager-install.sh: Installing cloudera-manager-server package..."
sudo apt-get -q --force-yes -y install cloudera-manager-server || error_exit $? "Failed to install cloudera-manager-server"


## installing postgresql package --- to avoid interactive screen when installing the db-server
echo "cloudera-manager-install.sh: Installing postgresql package..."
sudo apt-get -q --force-yes -y install postgresql || error_exit $? "Failed to install postgresql"


## installing cloudera-manager-server-db package
echo "cloudera-manager-install.sh: Installing cloudera-manager-server-db package..."
sudo apt-get -q --force-yes -y install cloudera-manager-server-db || error_exit $? "Failed to install cloudera-manager-server-db"


echo "cloudera-manager-install.sh: cm4 packages are installed!..."

