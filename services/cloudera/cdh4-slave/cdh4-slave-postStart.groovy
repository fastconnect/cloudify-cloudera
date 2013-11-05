/*******************************************************************************
 * postStart script for cdh4 slave host
 *******************************************************************************/

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.cloudifysource.dsl.context.ServiceContextFactory


def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("cdh4-slave-service.properties").toURL())
def builder = new AntBuilder()
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
def state = "SLAVE"
def baseConfDir = "${context.serviceDirectory}/services"
def services = context.attributes[managerServiceName].services
def hostAlias = context.attributes.thisInstance.hostAlias
def ipAddress = context.attributes.thisInstance.ipAddress

println "cdh4-slave-postStart.groovy: trying to contact cloudera manager service.... "

def cm4Service = context.waitForService("cdh4-manager", 20, TimeUnit.SECONDS)

if(cm4Service==null) {
	println "cdh4-slave-postStart.groovy: no cloudera manager service found!! exiting the postStart routine.... "
	System.exit(0)
}

def cm4Ip = context.attributes[managerServiceName].ipAddress
def cm4HostName = context.attributes[managerServiceName].hostAlias

println "cdh4-slave-postStart.groovy: got cloudera manager service!! Hostname is ${cm4HostName} and Ip is ${cm4Ip}"
println "cdh4-slave-postStart.groovy: configuring the node for services. state is <${state}>  . "

//if mongodb service is used, add a shard
if(context.attributes.thisApplication.useMongo == true) {
	def cdh4MasterService = context.waitForService(masterServiceName, 60, TimeUnit.SECONDS)
	
	if(cdh4MasterService != null){
		def lockTimeoutException = true
		def maxAttempt = 60
		def nbLeftAttempt=maxAttempt		
		def mongoDPort = context.attributes.thisInstance.mongoDPort
		
		println "cdh4-slave-postStart.groovy: invoking custom command addMongoShard to add ${hostAlias} ${ipAddress} ${mongoDPort}"
		while(lockTimeoutException && nbLeftAttempt>0) {
			try{
				cdh4MasterService.getInstances()[0].invoke("addMongoShard", hostAlias as String, ipAddress as String, mongoDPort as String )
				println "cdh4-slave-postStart.groovy: Shard ${hostAlias} ${ipAddress} ${mongoDPort} added!!"
				lockTimeoutException = false
			}catch(TimeoutException tex) {
				nbLeftAttempt--
				println "cdh4-slave-postStart.groovy: Lock TimeOutException!! will retry after 3 second.  ${nbLeftAttempt} attempts left..."
				sleep 3000
			}
		}
		if(nbLeftAttempt==0) println "cdh4-slave-postStart.groovy: unable add the shard  ${hostAlias} ${ipAddress} ${mongoDPort} after ${maxAttempt} attempts!"

	} else  println "cdh4-slave-postStart.groovy: No Master found!!" 
}


if(services ==null || services.isEmpty()) {
	println "cdh4-slave-postStart.groovy: no services def found!! skipping configurations"
	//tagging the instance as running
	context.attributes.thisInstance.isReady = true
	System.exit(0)
}

//configuring the node on the manager
def args = [ state ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("cdh4-node-configNode.groovy"))

//tagging the instance as running
context.attributes.thisInstance.isReady = true

def cdh4SlaveService = context.waitForService(context.getServiceName(), 20, TimeUnit.SECONDS)

//if not the last instance, tagg this instance as ready nand exit the routine
if(context.getInstanceId() != cdh4SlaveService.getNumberOfPlannedInstances()) {return}


//only if it is the last instance: wait for all the others slaves to be ready
def slavesInstances=cdh4SlaveService.getInstances()
def nbLeft = cdh4SlaveService.getNumberOfPlannedInstances()
//while((slavesInstances = cdh4SlaveService.getInstances()).length  < nbLeft) {}

println "cdh4-slave-postStart.groovy: <${nbLeft}> slave instances to wait for!"
slavesInstances.each {
	println "cdh4-slave-postStart.groovy: waiting for instance <${context.attributes.thisService.instances[it.getInstanceId()].hostAlias}> to be ready... "
	while( !context.attributes.thisService.instances[it.getInstanceId()].isReady ) {}
	nbLeft--
	println "cdh4-slave-postStart.groovy: <${nbLeft}> slave instances left:!"
}

if(nbLeft > 0) {
	println "cdh4-slave-postStart.groovy: WARNING! nbOfPlanned = ${cdh4SlaveService.getNumberOfPlannedInstances()} and nbNonReady = ${nbLeft}"
	//System.exit(1)
}

//then setup and start all services
def setupStartScript
println "SERVICES ARES: ${services}"
services.each {
	println "cdh4-slave-configNode.groovy: About to setup and start the service<${it.key}:${it.value}>"
	setupStartScript = "${baseConfDir}/${it.key.toUpperCase()}/${it.key.toLowerCase()}-setup-start.groovy"
	evaluate(new File("${setupStartScript}"))
}

//deploy all client config
println "cdh4-node-configNode.groovy: deploying the client configurations "
evaluate(new File("deployClientConfig.groovy"))

//creating the anonymous directory
println "cdh4-node-configNode.groovy: about to create the anonymous dir..."
builder.sequential {
	exec(executable: "sudo", failonerror: "false"){
		arg(line:"-u hdfs")
		arg(line:"hadoop fs")
		arg(line:"-mkdir /user/anonymous")
	}
	exec(executable: "sudo", failonerror: "false"){
		arg(line:"-u hdfs")
		arg(line:"hadoop fs")
		arg(line:"-chmod a+rwx /user/anonymous")
	}
}
