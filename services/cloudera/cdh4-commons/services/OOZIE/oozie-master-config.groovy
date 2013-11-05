import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a oozie master
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

def role = "OOZIE_SERVER"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/OOZIE/oozie-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def roleName
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.OOZIE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "oozie-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "oozie-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}


//name
roleName = service+"-"+role.toLowerCase()+"-"+instanceId

//adding the oowie server
println "mapreduce-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, role, hostname, roleName)) ){
	println "mapreduce-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... exiting"
	System.exit(1)
}


