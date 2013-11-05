import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hive master
 *
 * @param hostname
 * @param ip
 * @param instanceId
 *
 *******************************************************************************/
/*params*/
def hostname = args[0] as String
def ip = args[1] as String
def instanceId = args[2] as String

def roleServer2 = "HIVESERVER2"
def role ="HIVEMETASTORE"


def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HIVE/hive-config.properties").toURL())
def cmdSucceeded
def roleName
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
Binding execContext
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HIVE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hive-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hive-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
roleName = service+"-"+role.toLowerCase()+"-"+instanceId

//Server2 adding
println "hive-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, role, hostname, roleName)) ){
	println "hive-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... exiting"
	System.exit(1)
}

//name
roleName = service+"-"+roleServer2.toLowerCase()+"-"+instanceId

//metastore adding
println "hive-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${roleServer2}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, roleServer2, hostname, roleName)) ){
	println "hive-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${roleServer2}) .... exiting"
	System.exit(1)
}

