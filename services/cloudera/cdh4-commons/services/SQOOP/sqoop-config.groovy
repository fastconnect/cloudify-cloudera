import org.cloudifysource.dsl.context.ServiceContextFactory


/*******************************************************************************
 * configure a sqoop service
 *
 * the conf are writen in the sqoop-config.properties
 * 
 *******************************************************************************/


def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/SQOOP/sqoop-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.SQOOP as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "sqoop-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "sqoop-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def sqoopConfigMap = [items:[[name:'mapreduce_yarn_service', value:services.MAPREDUCE]]]

if (!(cmdSucceeded = restCli.configService(cluster, service, "", sqoopConfigMap) )) {
	println "sqoop-config.groovy: failed to configure the sqoop service <${service}>... exiting"
	System.exit(1)
}