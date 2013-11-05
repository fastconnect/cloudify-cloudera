import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hue service
 *
 * the custom conf properties are writen in the hue-config.properties
 * 
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HUE/hue-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HUE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hue-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hue-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def hueConfigMap = [ 
	items: [
		[name:'oozie_service', value:services.OOZIE],
		[name:'hue_webhdfs', value:services.HDFS+'-namenode-1']
	]
]

if(services.HIVE)
	hueConfigMap.items.add([name:'hive_service', value:services.HIVE])

if(services.HBASE)
	hueConfigMap.items.add([name:'hbase_service', value:services.HBASE])

if(services.SQOOP)
	hueConfigMap.items.add([name:'sqoop_service', value:services.SQOOP])

if (!(cmdSucceeded = restCli.configService(cluster, service, "", hueConfigMap) )) {
	println "hue-config.groovy: failed to configure the hue service <${service}>... exiting"
	System.exit(1)
}
