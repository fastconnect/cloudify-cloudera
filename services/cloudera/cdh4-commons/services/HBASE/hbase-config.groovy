import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hbase service
 *
 * the conf are writen in the hbase-config.properties
 *
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HBASE/hbase-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HBASE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hbase-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hbase-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def hbaseConfigMap = [items:[[name:'hdfs_service', value:services.HDFS],
							 [name:'zookeeper_service', value:services.ZOOKEEPER],
							 [name:'hdfs_rootdir', value:'/hbase']]]
if (!(cmdSucceeded = restCli.configService(cluster, service, "", hbaseConfigMap) )) {
	println "hbase-config.groovy: failed to configure the HBASE service <${service}>... exiting"
	System.exit(1)
}
