import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a mapreduce master
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

def jtRole = "JOBTRACKER"
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/MAPREDUCE/mapreduce-config.properties").toURL())
def cmdSucceeded
def jtRoleName
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
println "mapreduce-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "mapreduce-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
jtRoleName = service+"-"+jtRole.toLowerCase()+"-"+instanceId

//job tracker adding
println "mapreduce-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${jtRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, jtRole, hostname, jtRoleName)) ){
	println "mapreduce-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${jtRole}) .... exiting"
	System.exit(1)
}

//job tracker conf
def jtConfigMap = [items:[[name:'jobtracker_mapred_local_dir_list', value:'/mapred/jt'], 
						  [name:'mapred_job_tracker_handler_count', value:config.mrJtHandlerCount]]]

println "mapreduce-master-config.groovy: about to configure the jobtracker node: properties ares: ${jtConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, jtRoleName, jtConfigMap)) ){
	println "mapreduce-master-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${jtRoleName}) .... exiting "
	System.exit(1)
}
