/*******************************************************************************
 * stop script for cdh4 node host
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory
import com.gmongo.GMongo
import com.mongodb.CommandResult;
import com.mongodb.Mongo;
import com.mongodb.DB;

def context = ServiceContextFactory.getServiceContext()
def builder = new AntBuilder()
def config = new ConfigSlurper().parse(new File("${context.serviceName}-service.properties").toURL())
def stopScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-stop.sh"

//tagging the instance as not running
context.attributes.thisInstance.isReady = false

builder.sequential {
	echo(message:"cdh4-node-stop.groovy: Running ${stopScriptSh}...")
	chmod(file: stopScriptSh, perm:"ugo+rx")
	exec(executable: stopScriptSh,failonerror: "true")
}

//case use of mongo
if(context.attributes.thisApplication.useMongo == true) {

	// read the mongod.lock file to retrieve the PID to kill it?
	// or run the shutdown command?
	
	// retrieves the differents ports//
	//slave
	if(context.attributes.thisInstance.mongoDPort) {
		mongoDPort=context.attributes.thisInstance.mongoDPort
	}else{
		mongoDPort=config.mongoDPort
	}
	
	//master
	if(context.attributes.thisInstance.mongoConfigPort) {
		mongoConfigPort=context.attributes.thisInstance.mongoConfigPort
	}else{
		mongoConfigPort=config.mongoConfigPort
	}
	if(context.attributes.thisInstance.mongoSPort) {
		mongoSPort=context.attributes.thisInstance.mongoSPort
	}else{
		mongoSPort=config.mongoSPort
	}
	
	mongo = new GMongo("127.0.0.1", mongoDPort)
	admin = mongo.getDB("admin")

	result = admin.command(["shutdown" : "1", "force" : "true", "timeoutSecs" : 15])
	result = admin.command(cmd)

	println "mongod_stop.groovy: result is ${result}"
	if(!result.ok) {

		datadir = serviceContext.attributes.thisInstance["datadir"]
		pid = new File("${datadir}/mongod.lock").readLines()[0].toInteger()

		new AntBuilder().sequential {
			exec(executable:"kill", output:"${home}/mongodb_stop.log", osfamily:"unix") {
				arg line:"-9"
				arg line:"${pid}"
			}
			exec(executable:"taskkill", output:"${home}/mongodb_stop.log", osfamily:"windows") {
				arg line:"/PID"
				arg line:"${pid}"
			}
		}
	}
}

/*println "cdh4-node-stop.groovy: Stopping cloudera manager agents and its manage processes "
 builder.sequential {
 exec(executable: "sudo", osfamily:"unix", failonerror:"true") {
 arg(line:"service cloudera-scm-agent hard_stop")
 }
 }*/

/* println "cdh4-node-stop.groovy: trying to contact cloudera manager service.... "
 def cm4Service = context.waitForService("cloudera-manager", 20, TimeUnit.SECONDS)
 if(cm4Service==null) {
 println "cdh4-node-stop.groovy: no cloudera manager service found!! exiting the stop routine.... "
 System.exit(0)
 }
 def cm4Instance = cm4Service.waitForInstances(1, 40, TimeUnit.SECONDS)[0]
 def cm4Ip = cm4Instance.getHostAddress()
 def cm4HostName = cm4Instance.getHostName()
 def currentInstance = (context.waitForService(context.getServiceName(), 20, TimeUnit.SECONDS).getInstances().find{ it.instanceId == context.instanceId }) as ServiceInstance
 println "cdh4-node-stop.groovy: got cloudera manager service!! Hostname is ${cm4HostName} and Ip is ${cm4Ip}"
 println "cdh4-node-stop.groovy: invoking removeNode command of manager: host<${currentInstance.getHostName()}>"
 //removing of the cluster
 //cm4Instance.invoke("removeNode", currentInstance.getHostName() as String)
 */