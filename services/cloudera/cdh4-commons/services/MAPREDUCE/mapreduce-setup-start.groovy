import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 *  setup all the dependencies of mapreduce before (re)starting the service
 *
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/MAPREDUCE/mapreduce-config.properties").toURL())
def builder = new AntBuilder()
def cmdSucceeded
CM4RESTClient restCli
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.MAPREDUCE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "mapreduce-setup-start.groovy: api url is: ${restCli.apiUrl}..."
//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "mapreduce-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}

//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))
