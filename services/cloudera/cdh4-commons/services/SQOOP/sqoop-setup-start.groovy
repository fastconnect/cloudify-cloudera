import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of sqoop before (re)starting the service
 *
 *
 *******************************************************************************/
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/SQOOP/sqoop-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.SQOOP as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "sqoop-setup-start.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "sqoop-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}

if (context.attributes[managerServiceName].containsKey("${cluster}.${service}.isStarted") ) {
	if ((context.attributes[managerServiceName]["${cluster}.${service}.isStarted"] as boolean)==true) {
		println "sqoop-setup-start.groovy: sqoop service already started!! skipping..."
		return
	}
}


//create the sqoop user directory
println "sqoop-setup-start.groovy: about to create the  sqoop user directory..."
def isUserDirCreated = false
if (context.attributes[managerServiceName].containsKey("${service}.isUserDirCreated") ) {
	isUserDirCreated = context.attributes[managerServiceName]["${service}.isUserDirCreated"] as boolean
}
if( isUserDirCreated ) {
	println "sqoop-setup-start.groovy: sqoop database already created!!!..."
}else if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_CREATE_SQOOP_USER_DIR, null)) ){
	println "sqoop-setup-start.groovy: Failed to create sqoop user directory... exiting "
	System.exit(1)
}else{
	context.attributes[managerServiceName]["${service}.isUserDirCreated"] = true
}


//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))
