# cdh4-manager

**Type**		: SERVICE  
**Status**		: TESTED
**Description**	: cloudera manager recipe 1.0.0   
**Maintainer**	: Fastconnect  
**Maintainer email**	:   
**Contributors**		: 
**Homepage**			:  
**License**				: Apache 2.0    
**Build**: 
**Linux sudoer permissions**	: REQUIRED   
**Windows Admin permissions**	: Not supported      
**Release Date**				: July 26th 2013

## Recipe version

* **Date** : Month Day Year (tag creation date)
	* **Tag/Branch Name** 	: cloudera manager recipe 1.0.0  
	* **Recipe Base URL**   : [https://fastconnect.org/svn/FastConnect/projects/cloudify/recipes](https://fastconnect.org/svn/FastConnect/projects/cloudify/recipes)

## Tested Cloud & OS

* **Amazon EC2 Cloud**
	* **precise64** : Ubuntu precise 64 bits

* **Virtual Box Cloud**
	* **precise64** : Ubuntu precise 64 bits

## Details

### Service dependencies

Dependencies between services <> applications

* **Service 1** :
	* **Service TAG** 	: wich tag you're using for this application version
	* **Details**		: .....
* **Service 2** :
	* **Service TAG** 	: wich tag you're using for this application version
	* **Details**		: .....

### Service description
This folder contains a service recipe for cloudera manager 4.

Cloudera manager Version tested: 4.6.1

The Cloudera manager is available on the installed node at the port 7180
The Hue interface is available on the installed node (MasterNode) at the port 8888

### Properties file

* **Property name** : *port*
	* REQUIRED
	* **Description** :the http port for the cloudera manager web interface
	
* **Property name** : *apiVersion*
	* REQUIRED
	* **Description** : The cm4 api version to use. Default to 4
	
* **Property name** : *cluster*
	* REQUIRED
	* **Description** : The cluster name to install

* **Property name** : *services*
	* REQUIRED
	* **Description** : an array of cloudera services to install and configure on the cluster

* **Property name** : *clouderaHadoopVersion*
	* REQUIRED
	* **Description** : cloudera Hadoop version (cdh4 version): supported and tested version is 4.3.0


* **Property name** : *clouderaManagerVersion*
	* REQUIRED
	* **Description** : cloudera manager version (cm4 version): supported and tested version is 4.6.1

	
### Custom commands

* **Command Name** 	: *displayHosts*
	* **Action** 	: displays couple ipAddress - hostName of all hosts in the cluster.
	* **Usage**		: invoke cdh4-manager displayHosts [ipType]
		**Args**		ipType 	==> the ip type display. values are "private" for privates ip ; "public" for public ip. default to public
	* **Example**	: invoke cdh4-manager displayHosts private
					  invoke cdh4-manager displayHosts 					#displays public ips

## Changelist
