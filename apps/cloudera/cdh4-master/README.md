# cdh4-master

**Type**		: SERVICE  
**Status**		: TESTED  
**Description**		: cloudera cdh4 master recipe 1.0.0   
**Maintainer**		: Fastconnect  
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
	* **Tag/Branch Name** 	: cloudera cdh4 master recipe 1.0.0 
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
	* **Details**		: cdh4-master depends on cdh4-manager


### Service description
This folder contains an application recipe for cloudera cdh4.
manages installation of packages and configuration of master roles such as JobTracker, NameNode, HueServer, Oozie server, ... 
Cloudera Services to configure are defined in the properties file of cdh4-manager service.

### Properties file

* **Property name** : *agentServerPort*
	* REQUIRED
	* **Description** :Default is *7182*.
	
* **Property name** : *agentListeningPort*
	* REQUIRED
	* **Description** :Default is *9000*

* **Property name** : *useMongo*
	* REQUIRED
	* **Description** :enable(intall and configure) or disable the use of a mongoDB cluster

* **Property name** : *mongoConfigPort*
	* REQUIRED
	* **Description** :the port for the mongo Config instance. Default is *27019*

* **Property name** : *mongoSPort*
	* REQUIRED
	* **Description** :the port for the mongoS instance. Default is *27017*

* **Property name** : *clouderaHadoopVersion*
	* REQUIRED
	* **Description** : cloudera Hadoop version (cdh4 version): supported and tested version is *4.3.0*


* **Property name** : *clouderaManagerVersion*
	* REQUIRED
	* **Description** : cloudera manager version (cm4 version): supported and tested version is *4.6.1*

* **Property name** : *aws*
	* REQUIRED
	* **Description** : amazon Web Services configurations. 

### Custom commands

* **Command Name** 	: *addMongoShard*
	* **Action** 	: add a shard to the existing mongo cluster
	* **Usage**		: *invoke cdh4-master addMongoShard  <hostname>  <ipAddress> <port>*
	* **Args**	:	
		* ***hostname*** 	==> the hostname of the new shard ; <br>
		* ***ipAddress*** 	==> the Ip Address of the new shard ;<br>
		* ***port*** 		==> the port on which to bind the new shard <br>
	* **Example**	: **invoke cdh4-master addMongoShard  "shard"  "196.0.0.2" "27020"**

* **Command Name** 	: *shardDataBase*
	* **Action** 	: Shard a mongo database
	* **Usage**		: *invoke cdh4-master shardDataBase  <databaseName>*  
	* **Args**	:	
		* ***databaseName*** 	==> the databse to shard 
	* **Example**	: **invoke cdh4-master shardDataBase  "myDatabase"**

* **Command Name** 	: *shardCollection*
	* **Action** 	: Shard a mongo collection. The sharding is done using the hash pattern on the provided key
	* **Usage**		: *invoke cdh4-master shardCollection  <databaseName>  <collectionName> <shardKey>*
	* **Args**	:		
		* ***databaseName*** 	==> the database containing the collection to shard. It must be a sharded one.
		* ***collectionName*** 	==> the collection to shard 
		* ***shardKey*** 		==> the sharding key. 
	* **Example**	: **invoke cdh4-master shardCollection  "myDatabase"  "oneCollection" "id"**

* **Command Name** 	: *updateCoreSiteXml*
	* **Action** 	: update the "/etc/hadoop/conf/core-site.xml" file to add Amazon credentials. To be run before backing up or restoring from S3
	* **Usage**		: *invoke chd4-master updateCoreSiteXml*
	* **Example**	: **invoke chd4-master updateCoreSiteXml**
	
* **Command Name** 	: *s3HdfsBackup*
	* **Action** 	: backup a hdfs directory into the specify folder in an AmazonS3 bucket.
	* **Usage**		: *invoke cdh4-master s3HdfsBackup [hdfsSourcePath] [s3DestForlder]*
	* **Args**	:	
		* ***hdfsSourcePath*** 	==> the hdfs folder to backup. if not provided, will backup the entire hdfs file sys ("/" forlder)
		* ***s3DestForlder*** 	==> the Amazon  s3 folder in which to store the backup. if not provided, will store into a s3 folder named like "hdfs-backup-yyy-mm-dd~hh.mm.ss"
	* **Examples**	: 
		* **invoke cdh4-master s3HdfsBackup "/test" "backup1"**  *#will backup /test folder into "backup1" folder in s3*
		* **invoke cdh4-master s3HdfsBackup "/test"**			 *#will backup /test folder into "hdfs-backup-yyy-mm-dd~hh.mm.ss" in s3*
		* **invoke cdh4-master s3HdfsBackup**				 *#will backup the hole file system ( "/" folder ) into "hdfs-backup-yyy-mm-dd~hh.mm.ss" in s3*

* **Command Name** 	: *s3HdfsRestore*
	* **Action** 	: restore a hdfs directory from Amazon s3 backup
	* **Usage**		: *invoke cdh4-master s3HdfsRestore <sourceUrl> [hdfsDestPath]*
	* **Args**    	:
		* ***sourceUrl ***		==> the S3 backup to restore
		* ***hdfsDestPath*** 	==> the hdfs destination folder in which to restore. if not provided, will backup the entire hdfs file sys ("/" forlder)
		* ***s3DestForlder*** 	==> the Amazon  s3 folder in which to store the backup. if not provided, will restore into "/tmp/hdfs-restore-yyy-mm-dd~hh.mm.ss"
	* **Examples**	: 
		* **invoke cdh4-master s3HdfsRestore "backup1"  "/user/ubuntu/firstRestore"** *#will restore the s3 backup "backup1" into hdfs "/user/ubuntu/firstRestore"*
		* **invoke cdh4-master s3HdfsRestore "backup1"**	  *#will restore the s3 backup "backup1" into hdfs "/tmp/hdfs-restore-yyy-mm-dd~hh.mm.ss"*

					  
## Changelist
