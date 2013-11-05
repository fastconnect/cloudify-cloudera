# cloudera

**Type**		: APPLICATION  
**Status**		: TESTED
**Description**	: cloudera recipe 1.0.0   
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
	* **Tag/Branch Name** 	: cloudera recipe 1.0.0 
	* **Recipe Base URL**   : [https://fastconnect.org/svn/FastConnect/projects/cloudify/recipes](https://fastconnect.org/svn/FastConnect/projects/cloudify/recipes)

## Tested Cloud & OS

* **Amazon EC2**
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
This folder contains an application recipe for cloudera cdh4 and cm4.

The Cloudera manager is available on the installed node at the port 7180
The Hue interface is available on the installed node (MasterNode) at the port 8888

Via the different services configurations files in the application folder, you can specify various configurations:
	- Cloudera cluster name to create
	- Cloudera services to install and configure
	- The use (set up) or not of mongoDb cluster
	- various ports configurations
Custom commands are also available from services. For more details, refer to the documentation.

### Properties file

* **Property name** : *myproperty1*
	* REQUIRED | NOT_REQUIRED
	* **Description** : why this property
* **Property name** : *myproperty2*
	* REQUIRED | NOT_REQUIRED
	* **Description** : why this property

### Custom commands


## Changelist