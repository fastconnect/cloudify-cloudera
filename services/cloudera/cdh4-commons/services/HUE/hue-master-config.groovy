import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hue master
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

def hsRole = "HUE_SERVER"
def bwsRole = "BEESWAX_SERVER"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HUE/hue-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def roleName
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HUE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hue-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hue-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}


//names
hsRoleName = service+"-"+hsRole.toLowerCase()+"-"+instanceId
bwsRoleName = service+"-"+bwsRole.toLowerCase()+"-"+instanceId

//hue server adding
println "hue-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${hsRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, hsRole, hostname, hsRoleName)) ){
	println "hue-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${hsRole}) .... exiting"
	System.exit(1)
}

//beeswax server adding
println "hue-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${bwsRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, bwsRole, hostname, bwsRoleName)) ){
	println "hue-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${bwsRole}) .... exiting"
	System.exit(1)
}



