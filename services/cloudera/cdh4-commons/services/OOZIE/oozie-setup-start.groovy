import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of oozie before (re)starting the service
 *
 *
 *******************************************************************************/
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/OOZIE/oozie-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.OOZIE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "oozie-setup-start.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "oozie-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}

if (context.attributes[managerServiceName].containsKey("${cluster}.${service}.isStarted") ) {
	if ((context.attributes[managerServiceName]["${cluster}.${service}.isStarted"] as boolean)==true) {
		println "oozie-setup-start.groovy: oozie service already started!! skipping..."
		return
	}
}


//create the database for oozie
println "oozie-setup-start.groovy: about to create the oozie database..."
def isDbCreated = false
if (context.attributes[managerServiceName].containsKey("${service}.isDbCreated") ) {
	isDbCreated = context.attributes[managerServiceName]["${service}.isDbCreated"] as boolean
}
if( isDbCreated ) {
	println "oozie-setup-start.groovy: oozie database already created!!!..."
}else if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_CREATE_OOZIE_DB, null)) ){
	println "oozie-setup-start.groovy: Failed to create the oozie database... exiting "
	System.exit(1)
}else{
	context.attributes[managerServiceName]["${service}.isDbCreated"] = true
}


//installing the shared lib
println "oozie-setup-start.groovy: about to install the oozie shared lib..."
if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_INSTALL_OOZIE_SHARE_LIB, null)) ){
	println "oozie-setup-start.groovy: Failed to install the oozie shared lib... exiting "
	System.exit(1)
}

//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))
