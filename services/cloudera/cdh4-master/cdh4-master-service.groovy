import org.cloudifysource.dsl.LifecycleEvents;
import org.cloudifysource.dsl.utils.ServiceUtils

service {
	extend "../cdh4-node"
	name "cdh4-master"
	numInstances 1
	elastic false

	compute {
		// http://fastconnect.org/pub/cloudify/boxes/precise64-cdh4.box
		template "CDH4_NODE_LINUX"
	}

	lifecycle{

		start "cdh4-master-start.groovy"
		
		startDetection {

			def test = ServiceUtils.isPortOccupied("${context.privateAddress}",agentListeningPort)
			println "IS PORT OCCUPIED (cm4-agent)${agentListeningPort} on ${context.privateAddress}--"+test
			if(!test) return false

			//case using mongo
			if (context.attributes.thisApplication.useMongo == true){
				def ports = []
				ports.add(context.attributes.thisInstance["mongoConfigPort"])
				ports.add(context.attributes.thisInstance["mongoSPort"])
				test = ServiceUtils.arePortsOccupied(ports)
				println "ARE PORTS OCCUPIED (monogC & mongoS)${ports} on localhost--"+test
			}
			return test
		}
	}


	customCommands ([
		
		/*
		 * ex of usage: invoke cdh4-master addMongoShard    hostname    ip    port
		 */
		"addMongoShard" :"cdh4-master-addMongoShard.groovy",
		
		/*
		 * ex of usage: invoke cdh4-master shardDataBase    databaseName
		 */
		"shardDataBase":"cdh4-master-shardDataBase.groovy",
		
		/*
		 * ex of usage: invoke cdh4-master shardCollection    databaseName    collectionName    shardKey
		 */
		"shardCollection":"cdh4-master-shardCollection.groovy",
		
		
		/*
		 * restore a hdfs directory from s3
		 * 
		 * usage: 		invoke cdh4-master s3HdfsRestore <sourceUrl> [hdfsDestPath]
		 * ex of usage: invoke cdh4-master s3HdfsRestore /test/backup1
		 * 
		 */
		"s3HdfsRestore":"cdh4-master-s3HdfsRestore.groovy",
		
				
		/*
		 * backup the "/" hdfs directory into the specify folder. if not defined, will store into 
		 * 
		 * usage: 		invoke cdh4-master s3HdfsBackup [hdfsSourcePath] [s3DestForlder]				//export the hdfs [hdfsSourcePath] into the [s3DestFolder]. 
		 * if s3DestForlder is not specified, will create a random name dir to store backups
		 * ex of usage: invoke cdh4-master s3HdfsBackup /test						// will save the "/test" dir into a folder named like "hdfs-backup-yyy-mm-dd~hh.mm.ss"
		 * 	
		 */		
		"s3HdfsBackup":"cdh4-master-s3HdfsBackup.groovy"
		
		
	])

}