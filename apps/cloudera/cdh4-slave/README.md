# cdh4-slave

**Type**		: SERVICE  
**Status**		: TESTED  
**Description**	: cloudera cdh4 slave recipe 1.0.0   
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
	* **Tag/Branch Name** 	: cloudera cdh4 slave recipe 1.0.0 
	* **Recipe Base URL**   : [https://github.com/fastconnect/cloudify-cloudera](https://github.com/fastconnect/cloudify-cloudera)

## Tested Cloud & OS

* **Amazon EC2**
	* **precise64** : Ubuntu precise 64 bits
	* **ImageID**	: eu-west-1/ami-fc7a6e88


## Details

### Service dependencies

Dependencies between services <> applications

* **cdh4-manager** :
	* **Service TAG** 	: current version
	* **Details**		: cdh4-slave depends on cdh4-manager

* **cdh4-master** :
	* **Service TAG** 	: current version
	* **Details**		: cdh4-slave depends on cdh4-master

### Service description
This folder contains an application recipe for cloudera cdh4.
manages installation of packages and configuration of slaves roles such as Tasktracker and DataNode... 
cloudera Services to configure are defined in the properties file of cdh4-manager service.
Also configure nodes as mongoDB shards if necessary

### Properties file

* **Property name** : *agentServerPort*
	* REQUIRED
	* **Description** :Default is *7182*.
	
* **Property name** : *agentListeningPort*
	* REQUIRED
	* **Description** :Default is *9000*
	
* **Property name** : *mongoDPort*
	* REQUIRED
	* **Description** :the port for the mongoD instance. Default is *27018*

* **Property name** : *clouderaHadoopVersion*
	* REQUIRED
	* **Description** : cloudera Hadoop version (cdh4 version): supported and tested version is *4.3.0*


* **Property name** : *clouderaManagerVersion*
	* REQUIRED
	* **Description** : cloudera manager version (cm4 version): supported and tested version is *4.6.1*

### Custom commands
None
					  
## Changelist
