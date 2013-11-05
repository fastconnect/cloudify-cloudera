import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hive service
 *
 * the custom conf properties are writen in the hive-config.properties
 * 
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HIVE/hive-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HIVE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hive-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hive-config.groovy: REST API not available! exiting"
	System.exit(1)
}

def dbHostName = context.attributes[masterServiceName].instances[1].hostAlias
if(dbHostName==null || dbHostName.isEmpty()) {
	println "hive-config.groovy: No master found! setting the host DB to locahost"
	dbHostName = "localhost"
}

def hiveConfigMap = [items:[[name:'hive_metastore_database_host', value:dbHostName],
							[name:'mapreduce_yarn_service', value:services.MAPREDUCE],
							[name:'zookeeper_service', value:services.ZOOKEEPER],
							[name:'hive_metastore_database_type', value:'derby'],
							[name:'hive_metastore_database_auto_create_schema', value:true]]]


if (!(cmdSucceeded = restCli.configService(cluster, service, "", hiveConfigMap) )) {
	println "hive-config.groovy: failed to configure the hive service <${service}>... exiting"
	System.exit(1)
}


