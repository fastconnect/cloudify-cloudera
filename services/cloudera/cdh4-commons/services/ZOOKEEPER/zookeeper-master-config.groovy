import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a zookeeper master(the server) 
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

def serverRole = "SERVER"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/ZOOKEEPER/zookeeper-config.properties").toURL())
def cmdSucceeded
def serverRoleName
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.ZOOKEEPER as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "zookeeper-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availability of the api
if(!restCli.isApiAvailable(60)){
	println "zookeeper-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//names
serverRoleName = service+"-"+serverRole.toLowerCase()+"-"+instanceId

//server role adding
println "zookeeper-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${serverRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, serverRole, hostname, serverRoleName)) ){
	println "zookeeper-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${serverRole}) .... exiting"
	System.exit(1)
}

// configuration of roles
//server node conf
def serverConfigMap = [items:[[name:'dataDir', value:'/var/lib/zookeeper'], 
						  [name:'dataLogDir', value:'/var/lib/zookeeper'],
						  [name:'maxSessionTimeout', value:config.maxSessionTimeOut]]]

println "zookeeper-master-config.groovy: about to configure the zookeeper server: properties ares: ${serverConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, serverRoleName, serverConfigMap)) ){
	println "zookeeper-master-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${serverRoleName}) .... exiting "
	System.exit(1)
}

