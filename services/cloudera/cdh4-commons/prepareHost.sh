#!/bin/bash

cm4repofile="cloudera-cm4.list"
cdh4repofile="cloudera-cdh4.list"
mongodbrepofile="10gen.list"
ubuntuDist=$1
clouderaCm4Version=$2
clouderaCdh4Version=$3

# installing curl
echo "prepareHost.sh: installing curl package..."
sudo apt-get -q --force-yes -y install curl 

## cm4 repository
if [ -f "/etc/apt/source.list.d/cloudera-cm4.list" ]
then
    sudo rm /etc/apt/source.list.d/$cm4repofile
fi
echo "prepareHost.sh: adding cm4 repository..."
echo -e "deb [arch=amd64] http://archive.cloudera.com/cm4/ubuntu/$ubuntuDist/amd64/cm $ubuntuDist-cm$clouderaCm4Version contrib \n deb-src http://archive.cloudera.com/cm4/ubuntu/$ubuntuDist/amd64/cm $ubuntuDist-cm$clouderaCm4Version contrib" > $cm4repofile
sudo cp $cm4repofile /etc/apt/sources.list.d/
sudo curl -s http://archive.cloudera.com/cm4/ubuntu/$ubuntuDist/amd64/cm/archive.key | sudo apt-key add -

## cdh4 repository
if [ -f "/etc/apt/source.list.d/cloudera-cdh4.list" ]
then
    sudo rm /etc/apt/source.list.d/$cdh4repofile
fi
echo "prepareHost.sh: adding cdh4 repository..."
echo -e "deb [arch=amd64] http://archive.cloudera.com/cdh4/ubuntu/$ubuntuDist/amd64/cdh $ubuntuDist-cdh$clouderaCdh4Version contrib \ndeb-src http://archive.cloudera.com/cdh4/ubuntu/$ubuntuDist/amd64/cdh $ubuntuDist-cdh$clouderaCdh4Version contrib" > $cdh4repofile
sudo cp $cdh4repofile /etc/apt/sources.list.d/
sudo curl -s http://archive.cloudera.com/cdh4/ubuntu/$ubuntuDist/amd64/cdh/archive.key | sudo apt-key add -

## mongodb-10gen repository
if [ -f "/etc/apt/source.list.d/10gen.list" ]
then
    sudo rm /etc/apt/source.list.d/$mongodbrepofile
fi
echo "prepareHost.sh: adding cdh4 repository..."
echo -e "deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen" > $mongodbrepofile
sudo cp $mongodbrepofile /etc/apt/sources.list.d/
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10

## updating the repo list
sudo apt-get update

##setting the javaHome

if [ ! -d "/usr/lib/jvm" ]
then
    sudo mkdir -p /usr/lib/jvm
fi

echo "prepareHost.sh: Installing new jdk 1.7"
javaUrl="https://s3-eu-west-1.amazonaws.com/cloudify-eu/jdk-7u21-linux-x64.tar.gz "
wget -nv -O $HOME/java-7-021.tar.gz  $javaUrl
sudo cp $HOME/java-7-021.tar.gz /usr/lib/jvm/
sudo tar xfz /usr/lib/jvm/java-7-021.tar.gz -C /usr/lib/jvm/
export JAVA_HOME=/usr/lib/jvm/jdk1.7.0_21
cp /etc/bash.bashrc $HOME
cp /etc/profile $HOME
echo -e "export JAVA_HOME=/usr/lib/jvm/jdk1.7.0_21\nexport PATH=$JAVA_HOME/bin:$PATH" >> $HOME/bash.bashrc
sudo cp $HOME/bash.bashrc /etc/bash.bashrc
source /etc/bash.bashrc
#source /etc/profile

echo "prepareHost.sh: creating link for java_home..."
## link for default jaba home

if [ -e "/usr/lib/jvm/default-java" ]
then
    sudo rm /usr/lib/jvm/default-java
fi

sudo ln -s $JAVA_HOME /usr/lib/jvm/default-java


exit