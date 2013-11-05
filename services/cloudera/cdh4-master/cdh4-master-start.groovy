/*******************************************************************************
 * start script for cdh4 master host
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils

def context = ServiceContextFactory.getServiceContext()
//def config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())
def builder = new AntBuilder()
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def startScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-start.sh"

builder.sequential {
	echo(message:"cdh4-master-start.groovy: Running ${startScriptSh}...")
	chmod(file: startScriptSh, perm:"ugo+rx")
	exec(executable: startScriptSh,failonerror: "true")
}


//case use of mongo
if(context.attributes.thisApplication.useMongo == true) {
	builder.sequential {
		exec (executable: "sudo") {
			arg(line:"service mongodb stop")
		}
	}

	def logsDir = "/var/log/mongodb"
	//mongo config
	def mongoConfigPort = context.attributes.thisInstance["mongoConfigPort"]
	def dataDir = "/var/lib/mongodb/configdb"
	println "cdh4-master-start.groovy: MongoConfig dataDir is ${dataDir}"
	println "cdh4-master-start.groovy: logs in ${logsDir}/mongoConfig.log"

	println "cdh4-master-start.groovy: starting the MongoConfig server using port ${mongoConfigPort}..."
	builder.sequential {
		//creating the data directory
		exec (executable: "sudo") {
			arg line: "mkdir -p ${dataDir} ${logsDir}"
		}
		exec(executable:"sudo") {
			//arg line:"--journal"
			arg value: "mongod"
			arg value:"--configsvr"
			arg line:"--dbpath \"${dataDir}\""
			arg line:"--port ${mongoConfigPort}"
			arg value: "--fork"
			arg line: "--logpath ${logsDir}/mongoConfig.log"
		}

		echo(message:"cdh4-master-start.groovy: after mongoConfig start")
	}

	for(int i=0; !ServiceUtils.isPortOccupied(mongoConfigPort) && i<100; i++){sleep 100}
	
	//mongoS
	def mongoSPort = context.attributes.thisInstance["mongoSPort"]
	def configdb = "${context.attributes.thisInstance.ipAddress}:${mongoConfigPort}"
	println "cdh4-master-start.groovy: starting the MongoS instance using port ${mongoSPort}..."
	println "cdh4-master-start.groovy: configServ are ${configdb}..."
	println "cdh4-master-start.groovy: logs in ${logsDir}/mongos.log"

	builder.sequential {
		exec(executable:"sudo") {
			arg value: "mongos"
			arg line:"--configdb ${configdb}"
			arg line:"--port ${mongoSPort}"
			arg value: "--fork"
			arg line: "--logpath ${logsDir}/mongos.log"
		}
	}
}
