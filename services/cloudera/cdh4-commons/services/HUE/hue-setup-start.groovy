import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of hue before (re)starting the service 
 *
 *******************************************************************************/



def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HUE/hue-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HUE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

//testing the availability of the api
if(!restCli.isApiAvailable(60)){
	println "hue-setup-start-config.groovy: api url is: ${restCli.apiUrl}..."
	println "hue-setup-start-config.groovy: REST API not available! exiting"
	System.exit(1)
}


if (context.attributes[managerServiceName].containsKey("${cluster}.${service}.isStarted") ) {
	if ((context.attributes[managerServiceName]["${cluster}.${service}.isStarted"] as boolean)==true) {
		println "hue-setup-start-config.groovy: hue service already started!! skipping..."
		return
	}
}

//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))

