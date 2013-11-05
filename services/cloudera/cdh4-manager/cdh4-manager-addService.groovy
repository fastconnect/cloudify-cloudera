import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * addService script for cloudera manager
 * 
 * @param service
 * @param serviceType
 * 
 *******************************************************************************/
/*params*/
def service = args[0] as String
def serviceType = (args[1] as String).toUpperCase()

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/cdh4-manager-service.properties").toURL())
def cmdSucceeded
Binding execContext
def cm4Ip = context.attributes.thisService.ipAddress
CM4RESTClient restCli
def cluster = context.attributes.thisService.cluster.name
def serviceConfDir = "${context.serviceDirectory}/services/${serviceType}"
def serviceConfScript = "${serviceConfDir}/${serviceType.toLowerCase()}-config.groovy"
//def serviceConfMasterScript = "${serviceConfDir}/${serviceType.toLowerCase()}-master-config.groovy"
//def serviceConfSlaveScript = "${serviceConfDir}/${serviceType.toLowerCase()}-slave-config.groovy"

restCli = new CM4RESTClient(cm4Ip, config.apiVersion)

println "cdh4-manager-addService.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "cdh4-manager-addService.groovy: REST API not available! exiting"
	System.exit(1)
}

println "cdh4-manager-addService.groovy: About to add a service <${serviceType}:${service}> to the cluster <${cluster}>.... "
if ( !(cmdSucceeded = restCli.addService(cluster, service, serviceType)) ) {
	println "cdh4-manager-addService.groovy: failed to add service <${serviceType}:${service}> to the cluster <${cluster}>... exiting"
	System.exit(1)
}

//saving the new service
def services = context.attributes.thisService.containsKey("services") ? context.attributes.thisService.services : [:]

println"cdh4-manager-addService.groovy: for service ${serviceType}:${service} => Old services map: ${services}"
services[serviceType] = service
context.attributes.thisService.services = services
println"cdh4-manager-addService.groovy: for service ${serviceType}:${service} => New services map: ${services}"

//configuring the service
println "cdh4-manager-addService.groovy: Trying to configure the <${serviceType}:${service}>"
evaluate(new File("${serviceConfScript}"))

////trying to configure the master
//println "cdh4-manager-addService.groovy: Trying to configure the master ..."
//def cdh4MasterService = context.waitForService("cdh4-master", 20, TimeUnit.SECONDS)
//if(cdh4MasterService!=null) {
//	def cdh4MasterInstances = cdh4MasterService.getInstances() //waitForInstances(1, 20, TimeUnit.SECONDS)
//	if(cdh4MasterInstances!=null) {
//		def isReady = context.attributes.'cdh4-master'.instances[cdh4MasterInstances[0].getInstanceId()].isReady
//		if(isReady==null || !isReady){
//			println "cdh4-manager-addService.groovy:(is ready = ${isReady} No ready master instance found.. skipping configuration "
//			return
//		}
//		
//		def args = ["${cdh4MasterInstances[0].getHostName()}", 
//					"${cdh4MasterInstances[0].getHostAddress()}", 
//					"${cdh4MasterInstances[0].getInstanceId()}" ] as String[]
//		execContext = new Binding(args)
//		new GroovyShell(execContext).evaluate(new File("${serviceConfMasterScript}"))
//	}else{
//		println "cdh4-manager-addService.groovy: No ready master instance found.. skipping configuration "
//		return
//	}
//}else{
//	println "cdh4-manager-addService.groovy: No cdh4-master service found.. skipping configuration "
//	return
//}
//
////trying to configure the slaves
//println "cdh4-manager-addService.groovy: Trying to configure the slaves ..."
//def cdh4SlaveService = context.waitForService("cdh4-slave", 20, TimeUnit.SECONDS)
//if(cdh4SlaveService!=null) {
//	def cdh4SlaveInstances = cdh4SlaveService.getInstances() //waitForInstances(cdh4SlaveService.getNumberOfPlannedInstances(), 20, TimeUnit.SECONDS)
//	if(cdh4SlaveInstances!=null) {
//		cdh4SlaveInstances.each {
//			def isReady = context.attributes.'cdh4-slave'.instances[it.getInstanceId()].isReady
//			if(isReady==null || !isReady){
//				println "cdh4-manager-addService.groovy: The slave instance(${it.getInstanceId()}) <${it.getHostName()}:${it.getHostAddress()}> not yet ready... skipping configuration "
//			}
//			
//			def args = ["${it.getHostName()}",
//						"${it.getHostAddress()}",
//						"${it.getInstanceId()}" ] as String[]
//			execContext = new Binding(args)
//			new GroovyShell(execContext).evaluate(new File("${serviceConfSlaveScript}"))
//		}	
//	}else{
//		println "cdh4-manager-addService.groovy: No ready slave instances found.. skipping configuration "
//	}
//}else{
//	println "cdh4-manager-addService.groovy: No cdh4-slave service found.. skipping configuration "
//}
//
////deploy all client config
//println "cdh4-manager-addService.groovy: deploying the client configurations "
//evaluate(new File("cdh4-manager-deployClientConfig.groovy"))
