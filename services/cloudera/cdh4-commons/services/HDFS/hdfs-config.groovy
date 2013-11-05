import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hdfs service
 *
 * the conf are writen in the hdfs-config.properties
 * 
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HDFS/hdfs-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HDFS as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hdfs-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hdfs-config.groovy: REST API not available! exiting"
	System.exit(1)
}


def dfsReplication  = config.dfsReplication
def slaveServ = context.waitForService(slaveServiceName, 10, TimeUnit.SECONDS)
if(slaveServ != null) {
	dfsReplication = dfsReplication > slaveServ.getNumberOfPlannedInstances()? slaveServ.getNumberOfPlannedInstances() : dfsReplication
}


def hdfsConfigMap = [items:[[name:'dfs_replication', value:dfsReplication],
							[name:'zookeeper_service', value:services.ZOOKEEPER]]]

if (!(cmdSucceeded = restCli.configService(cluster, service, "", hdfsConfigMap) )) {
	println "hdfs-config.groovy: failed to configure the hdfs service <${service}>... exiting"
	System.exit(1)
}