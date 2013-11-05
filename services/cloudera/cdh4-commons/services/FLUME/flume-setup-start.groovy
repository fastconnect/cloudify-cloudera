import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of flume before (re)starting the service
 *
 *
 *******************************************************************************/
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/FLUME/flume-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.FLUME as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "flume-setup-start.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "flume-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}
