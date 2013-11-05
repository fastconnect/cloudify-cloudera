import org.cloudifysource.dsl.context.ServiceContextFactory


/*******************************************************************************
 * configure a flume service
 *
 * the conf are writen in the flume-config.properties
 * 
 *******************************************************************************/


def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/FLUME/flume-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.FLUME as String

/*//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "flume-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "flume-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def flumeConfigMap = [items:[[name:'mapreduce_yarn_service', value:services.MAPREDUCE]]]

if (!(cmdSucceeded = restCli.configService(cluster, service, "", flumeConfigMap) )) {
	println "flume-config.groovy: failed to configure the flume service <${service}>... exiting"
	System.exit(1)
}*/