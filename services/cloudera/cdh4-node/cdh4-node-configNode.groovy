import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance

/*******************************************************************************
 * configNode script for a cdh4 node
 * 
 * @param state : the state of the newly added node: MASTER or SLAVE 
 * 
 *******************************************************************************/
/*params*/
def state = (args[0] as String).toUpperCase()

def hostname 
def ip 
def instanceId 

def context = ServiceContextFactory.getServiceContext()
Binding execContext
def managerServiceName = "cdh4-manager"
def services = context.attributes[managerServiceName].services
def baseConfDir = "${context.serviceDirectory}/services"
def serviceStateConfScript

if(services ==  null || services.size()<=0) {
	println "cdh4-node-configNode.groovy: No services definition found... skipping configurations "
	return
}

println "cdh4-node-configNode.groovy: services are: ${services}"

hostname = context.attributes.thisInstance.hostAlias
ip = context.attributes.thisInstance.ipAddress
instanceId = context.getInstanceId()

services.each {
	println "cdh4-node-configNode.groovy: About to add and configure a node to the cluster. properties are: host<${hostname}:${ip}>, service<${it.key}:${it.value}> , state<${state}> .... "
	serviceStateConfScript = "${baseConfDir}/${it.key.toUpperCase()}/${it.key.toLowerCase()}-${state.toLowerCase()}-config.groovy"
	def args = [ "${hostname}",
				 "${ip}",
				 "${instanceId}" ] as String[]
	execContext = new Binding(args)
	new GroovyShell(execContext).evaluate(new File("${serviceStateConfScript}"))
}

