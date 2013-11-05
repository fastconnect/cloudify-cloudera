/*******************************************************************************
 * custom command StartService script for cm4
 * 
 *
 * @param cluster
 * @param service
 *
 * 
 *******************************************************************************/
import org.cloudifysource.dsl.context.ServiceContextFactory


 /*params*/
 def cluster = args[0] as String
 def service = args[1] as String
 
 def cmdSucceeded
 CM4RESTClient restCli
 
 
 println "startService.groovy: About to start the service (${service}). cluster is (${cluster}) .... "
 
 def context = ServiceContextFactory.getServiceContext()
 def apiVersion = context.attributes.'cdh4-manager'.apiVersion
 def cm4Ip = context.attributes.'cdh4-manager'.ipAddress
 
 restCli = new CM4RESTClient(cm4Ip, apiVersion)
 
 println "startService.groovy: api url is: ${restCli.apiUrl}..."
 
 def builder = new AntBuilder()
 def isStarted = false
 def command = CM4RESTClient.CMD_START
 
 
if (context.attributes.'cdh4-manager'.containsKey("${cluster}.${service}.isStarted") ) {
		isStarted = context.attributes.'cdh4-manager'["${cluster}.${service}.isStarted"] as boolean
}
		 
if( isStarted ) {
		println "startService.groovy: ${service} already started!!! will try to restart..."
		command = CM4RESTClient.CMD_RESTART
}

cmdSucceeded = restCli.execServiceCommand(cluster, service, command, null)

context.attributes.'cdh4-manager'["${cluster}.${service}.isStarted"] = cmdSucceeded

if(!cmdSucceeded) {
	println "startService.groovy: Failed to ${command} service (${service}) of cluster (${cluster})..."
	return
}

println "startService.groovy: service (${service}) of cluster (${cluster}) ${command}ed."
 
 