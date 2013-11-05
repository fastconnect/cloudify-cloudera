import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of hive before (re)starting the service
 *
 *
 *******************************************************************************/
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HIVE/hive-config.properties").toURL())
def builder = new AntBuilder()
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HIVE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hive-setup-start.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hive-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}


if (context.attributes[managerServiceName].containsKey("${cluster}.${service}.isStarted") ) {
	if ((context.attributes[managerServiceName]["${cluster}.${service}.isStarted"] as boolean)==true) {
		println "hive-setup-start.groovy: hive service already started!! skipping..."
		return
	}
}

//warehouse creation
println "hive-setup-start.groovy: to create the warehouse directory..."
if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_HIVE_CREATE_WAREHOUSE, null)) ){
	println "hive-setup-start.groovy: Failed to to create the hive warehouse... exiting "
	System.exit(1)
}

//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))

/*//matastore db and tables creation
println "hive-setup-start.groovy: about to create the matastore Db and tables..."
if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_HIVE_CREATE_DB_AND_TABLES, null)) ){
	println "hive-setup-start.groovy: Failed to create the matastore Db and tables... exiting "
	System.exit(1)
}
*/

