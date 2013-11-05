import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a mapreduce service
 *
 * the conf are writen in the mapreduce-config.properties
 *
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/MAPREDUCE/mapreduce-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.MAPREDUCE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "mapreduce-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "mapreduce-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def mapreduceConfigMap = [items:[[name:'hdfs_service', value:services.HDFS],
								 [name:'zookeeper_service', value:services.ZOOKEEPER]]]

if (!(cmdSucceeded = restCli.configService(cluster, service, "", mapreduceConfigMap) )) {
	println "mapreduce-config.groovy: failed to configure the MAPREDUCE service <${service}>... exiting"
	System.exit(1)
}
