/*******************************************************************************
 * start script for cdh4 slave host
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
//def config = new ConfigSlurper().parse(new File("cdh4-slave-service.properties").toURL())
def builder = new AntBuilder()
def slaveServiceName = "cdh4-slave"
def managerServiceName = "cdh4-manager"
def startScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-start.sh"

builder.sequential {
	echo(message:"cdh4-slave-start.groovy: Running ${startScriptSh}...")
	chmod(file: startScriptSh, perm:"ugo+rx")
	exec(executable: startScriptSh,failonerror: "true")
}


//case use of mongo
if(context.attributes.thisApplication.useMongo == true) {
	def mongodPort = context.attributes.thisInstance.mongoDPort
	def logsDir = "/var/log/mongodb"
	def dataDir = "/var/lib/mongodb/db${context.instanceId}"
	context.attributes.thisInstance.mongoDataDir = dataDir
	context.attributes.thisInstance.mongoLogDir=logsDir
	builder.sequential {
		exec(executable:"sudo") {
			arg line:"service mongodb stop"
		}
		exec (executable: "sudo") {
			arg line: "mkdir -p ${dataDir} ${logsDir}"
		}
	}
	
	println "cdh4-slave-start.groovy: starting the Mongod instance using port ${mongodPort}..."
	println "cdh4-slave-start.groovy: data dir is ${dataDir}..."
	println "cdh4-slave-start.groovy: logs in ${logsDir}/db${context.instanceId}.log"
	builder.sequential{
		exec (executable: "sudo") {
			arg value: "mongod"
			arg line: "--dbpath ${dataDir}"
			arg line: "--port ${mongodPort}"
			arg value: "--fork"
			arg line: "--logpath ${logsDir}/db${context.instanceId}.log"
		}
	}

}
